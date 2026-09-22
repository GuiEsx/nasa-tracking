package exception;

public class ApiException extends RuntimeException {
    private final int codigoHttp;

    public ApiException(int codigoHttp, String mensagem) {
        super(mensagem);
        this.codigoHttp = codigoHttp;
    }

    public ApiException(String mensagem, Throwable causa) {
        super(mensagem, causa);
        this.codigoHttp = 0;
    }

    public int getCodigoHttp() {
        return codigoHttp;
    }
}

