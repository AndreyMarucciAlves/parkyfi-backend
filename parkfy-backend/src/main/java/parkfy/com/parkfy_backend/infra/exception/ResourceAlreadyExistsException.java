package parkfy.com.parkfy_backend.infra.exception;

//Exception criada para informar o client que já existe um campo igual a esse cadastrado
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String resource, Object field, Object value) {
        super(   "Não foi possível cadastras/alterar o(a) " + resource +
                 " porque já existe um(a) " + field +
                 " cadastrado com essa informação: " + value);
    }
}
