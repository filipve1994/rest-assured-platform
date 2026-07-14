package com.framework.assertions;


public class ResponseAssert {


private final ApiResponse response;



public ResponseAssert(
        ApiResponse response
){

this.response = response;

}



public ResponseAssert hasStatus(
        int expected
){

Assertions.assertThat(
        response.statusCode()
)
.isEqualTo(expected);


return this;

}



public ResponseAssert hasResponseTimeBelow(
        Duration duration
){

Assertions.assertThat(
        response.duration()
)
.isLessThan(
    duration.toMillis()
);


return this;

}


}