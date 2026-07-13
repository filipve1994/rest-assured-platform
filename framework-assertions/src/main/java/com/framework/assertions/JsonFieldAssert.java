package com.framework.assertions;


public class JsonFieldAssert {


private final Object value;



public JsonFieldAssert(
        ApiResponse response,
        String path
){

this.value =
    JsonPath.read(
        response.body(),
        path
    );

}



public JsonFieldAssert isEqualTo(
        Object expected
){

Assertions.assertThat(value)
        .isEqualTo(expected);


return this;

}



public JsonFieldAssert isNotBlank(){

Assertions.assertThat(value)
        .asString()
        .isNotBlank();


return this;

}



public JsonFieldAssert isPositive(){

Assertions.assertThat(
        (Number)value
)
.isPositive();


return this;

}


}