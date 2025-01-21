package ms_cuentas_bancarias.ms_cuentas_bancarias.utils;

import lombok.Data;

@Data
public class ClientResponse {

    private String id;
    private TypeClient type;
    private SubTypeClient subType;
    private String name;
    private TypeDocument typeDocument;
    private String nroDocument;
    private Boolean status;
}
