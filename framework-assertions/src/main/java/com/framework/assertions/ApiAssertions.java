package com.framework.assertions;

public final class ApiAssertions {


private ApiAssertions(){}



public static ResponseAssert assertThat(
        ApiResponse response
){

    return new ResponseAssert(response);

}


}