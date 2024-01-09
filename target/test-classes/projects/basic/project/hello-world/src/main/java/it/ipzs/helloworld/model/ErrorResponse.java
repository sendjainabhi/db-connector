
package it.ipzs.helloworld.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String errore;
    private String messaggio;
}
