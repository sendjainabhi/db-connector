#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.model;

import lombok.Data;

@Data
public class CreateOrUpdateProductDto {

    private String name;
}
