package com.talented.buttie.ledger.dto.request;

import com.talented.buttie.ledger.domain.TransactionType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Builder;


@Builder
public record UpdateTransactionRequestDTO(){

}
