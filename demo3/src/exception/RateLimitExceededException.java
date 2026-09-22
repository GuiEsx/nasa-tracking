package exception;

public class RateLimitExceededException extends ApiException {
    private final int tentativasRealizadas;
    private final long tempoTotalEsperaMs;

    public RateLimitExceededException(int tentativasRealizadas, String mensagem) {
        this(tentativasRealizadas, 0, mensagem);
    }

    public RateLimitExceededException(int tentativasRealizadas, long tempoTotalEsperaMs, String mensagem) {
        super(429, mensagem);
        this.tentativasRealizadas = tentativasRealizadas;
        this.tempoTotalEsperaMs = tempoTotalEsperaMs;
    }

    public int getTentativasRealizadas() {
        return tentativasRealizadas;
    }

    public long getTempoTotalEsperaMs() {
        return tempoTotalEsperaMs;
    }
}

