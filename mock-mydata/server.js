const jsonServer = require('json-server');
const db = require('./db.json');

const server = jsonServer.create();
const middlewares = jsonServer.defaults();
const port = Number(process.env.PORT || 3000);
const mockClient = {
  orgCode: 'MOCK001',
  clientId: 'buttie',
  clientSecret: 'mock-secret',
  redirectUri: 'http://localhost:8080/api/mydata/callback',
  appScheme: 'buttie'
};

server.use(middlewares);
server.use(jsonServer.bodyParser);

server.get('/health', (request, response) => response.jsonp({ status: 'UP' }));

function mockUserKey(request) {
  const authorization = request.get('Authorization') || '';
  const match = authorization.match(/^Bearer mock-access-token-user-(\d)$/);
  return match ? Number(match[1]) : null;
}

function userResponse(request, response, collection, transform = (body) => body) {
  const userKey = mockUserKey(request);
  if (userKey === null) {
    return response.status(401).jsonp({
      rsp_code: 'A0001',
      rsp_msg: '유효한 Mock 접근토큰이 필요합니다.'
    });
  }

  const body = collection.find((item) => item.id === userKey);
  if (!body) {
    return response.status(404).jsonp({
      rsp_code: 'A0404',
      rsp_msg: '사용자별 Mock 데이터를 찾을 수 없습니다.'
    });
  }
  return response.jsonp(transform(body));
}

server.get('/v2/oauth/2.0/authorize', (request, response) => {
  const userCi = request.get('x-user-ci') || '';
  const userId = Number(userCi);
  const state = request.query.state;
  const validRequest = request.query.org_code === mockClient.orgCode
    && request.query.response_type === 'code'
    && request.query.client_id === mockClient.clientId
    && request.query.redirect_uri === mockClient.redirectUri
    && request.query.app_scheme === mockClient.appScheme
    && Boolean(request.get('x-api-tran-id'));

  if (!Number.isSafeInteger(userId) || userId < 0 || !state || !validRequest) {
    return response.status(400).jsonp({
      rsp_code: 'A0002',
      rsp_msg: '인가 요청 필수값이 올바르지 않습니다.'
    });
  }

  const userKey = Math.abs(userId) % 10;
  return response.jsonp({
    code: `mock-auth-code-user-${userKey}`,
    state,
    api_tran_id: request.get('x-api-tran-id') || ''
  });
});

server.post('/v2/oauth/2.0/token', (request, response) => {
  const grantType = request.body && request.body.grant_type;
  const authorizationCode = request.body && request.body.code;
  const codeMatch = String(authorizationCode || '').match(/^mock-auth-code-user-(\d)$/);
  const validClient = request.body
    && request.body.org_code === mockClient.orgCode
    && request.body.client_id === mockClient.clientId
    && request.body.client_secret === mockClient.clientSecret
    && request.body.redirect_uri === mockClient.redirectUri;

  if (grantType !== 'authorization_code' || !codeMatch || !validClient) {
    return response.status(400).jsonp({
      error: 'invalid_grant',
      error_description: '유효한 Mock 인가코드가 필요합니다.'
    });
  }

  const userKey = Number(codeMatch[1]);
  const token = db.mydataTokens.find((item) => item.id === userKey);
  if (!token) {
    return response.status(404).jsonp({
      error: 'invalid_grant',
      error_description: '사용자별 Mock 토큰을 찾을 수 없습니다.'
    });
  }

  return response.jsonp({
    token_type: token.tokenType,
    access_token: token.accessToken,
    expires_in: token.expiresIn,
    refresh_token: token.refreshToken,
    refresh_token_expires_in: token.refreshTokenExpiresIn,
    scope: token.scope
  });
});

server.get('/v2/bank/accounts', (request, response) =>
  userResponse(
    request,
    response,
    db.bankAccountResponses,
    (body) => bankAccountsAt(body, seoulDateTime())
  )
);

server.get('/v2/card/cards', (request, response) =>
  userResponse(request, response, db.cardResponses)
);

function seoulDateTime() {
  if (/^\d{14}$/.test(process.env.MOCK_NOW || '')) {
    return process.env.MOCK_NOW;
  }
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Seoul',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hourCycle: 'h23'
  }).formatToParts(new Date());
  const values = Object.fromEntries(parts.map((part) => [part.type, part.value]));
  return `${values.year}${values.month}${values.day}`
    + `${values.hour}${values.minute}${values.second}`;
}

function isDate(value) {
  return /^\d{8}$/.test(String(value || ''));
}

function bankAccountsAt(body, now) {
  const originalTimestamp = /^\d{14}$/.test(String(body.search_timestamp || ''))
    ? body.search_timestamp
    : null;
  return {
    ...body,
    search_timestamp: now,
    account_list: (body.account_list || []).map((account) => ({
      ...account,
      balance_amt: accountBalanceAt(account, now, originalTimestamp)
    }))
  };
}

function accountBalanceAt(account, now, originalTimestamp) {
  if (originalTimestamp && now >= originalTimestamp) {
    return account.balance_amt;
  }

  const transactionBody = db.bankDepositTransactionResponses.find(
    (item) => item.id === account.account_num
  );
  const transactions = (transactionBody && transactionBody.trans_list) || [];
  const latest = transactions
    .filter((transaction) => /^\d{14}$/.test(String(transaction.trans_dtime || '')))
    .filter((transaction) => transaction.trans_dtime <= now)
    .sort((left, right) => right.trans_dtime.localeCompare(left.trans_dtime))[0];
  if (latest && Number.isFinite(latest.balance_amt)) {
    return latest.balance_amt;
  }

  const earliest = transactions
    .filter((transaction) => /^\d{14}$/.test(String(transaction.trans_dtime || '')))
    .sort((left, right) => left.trans_dtime.localeCompare(right.trans_dtime))[0];
  if (!earliest || !Number.isFinite(earliest.balance_amt)
    || !Number.isFinite(earliest.trans_amt)) {
    return account.balance_amt;
  }
  return earliest.trans_type === '01'
    ? earliest.balance_amt - earliest.trans_amt
    : earliest.balance_amt + earliest.trans_amt;
}

function approvalEventDateTime(approval) {
  if ((approval.status === '02' || approval.status === '03') && approval.trans_dtime) {
    return approval.trans_dtime;
  }
  return approval.approved_dtime;
}

server.get('/v2/card/cards/:cardId/approval-domestic', (request, response) => {
  const body = db.cardApprovalResponses.find((item) => item.id === request.params.cardId);
  if (!body) {
    return response.status(404).jsonp({ rsp_code: 'A0404', rsp_msg: '승인내역이 없습니다.' });
  }

  const now = seoulDateTime();
  const fromDate = isDate(request.query.from_date) ? request.query.from_date : '00000000';
  const requestedToDate = isDate(request.query.to_date)
    ? `${request.query.to_date}235959`
    : now;
  const effectiveToDateTime = requestedToDate < now ? requestedToDate : now;
  const fromDateTime = `${fromDate}000000`;
  const approvedList = (body.approved_list || []).filter((approval) => {
    const eventDateTime = approvalEventDateTime(approval);
    return /^\d{14}$/.test(String(eventDateTime || ''))
      && eventDateTime >= fromDateTime
      && eventDateTime <= effectiveToDateTime;
  });

  return response.jsonp({
    ...body,
    approved_cnt: approvedList.length,
    approved_list: approvedList
  });
});

server.get('/v2/card/cards/:cardId', (request, response) => {
  const body = db.cardBasicResponses.find((item) => item.id === request.params.cardId);
  return body
    ? response.jsonp(body)
    : response.status(404).jsonp({ rsp_code: 'A0404', rsp_msg: '카드 정보가 없습니다.' });
});

server.post('/v2/bank/accounts/deposit/transactions', (request, response) => {
  const accountNum = request.body && request.body.account_num;
  const body = db.bankDepositTransactionResponses.find((item) => item.id === accountNum);
  if (!body) {
    return response.status(404).jsonp({ rsp_code: 'A0404', rsp_msg: '계좌 거래내역이 없습니다.' });
  }

  const now = seoulDateTime();
  const fromDate = isDate(request.body && request.body.from_date)
    ? request.body.from_date
    : '00000000';
  const requestedToDate = isDate(request.body && request.body.to_date)
    ? `${request.body.to_date}235959`
    : now;
  const effectiveToDateTime = requestedToDate < now ? requestedToDate : now;
  const fromDateTime = `${fromDate}000000`;
  const transactionList = (body.trans_list || []).filter((transaction) => {
    const transactionDateTime = transaction.trans_dtime;
    return /^\d{14}$/.test(String(transactionDateTime || ''))
      && transactionDateTime >= fromDateTime
      && transactionDateTime <= effectiveToDateTime;
  });

  return response.jsonp({
    ...body,
    trans_cnt: transactionList.length,
    trans_list: transactionList
  });
});

server.listen(port, '0.0.0.0', () => {
  console.log(`Buttie MyData Mock Server is running on port ${port}`);
});
