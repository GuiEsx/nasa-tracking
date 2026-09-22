package model;

import java.time.LocalDateTime;

public interface Posicionavel {
    PosicaoAtual consultarPosicaoAgora();
    PosicaoAtual consultarPosicao(LocalDateTime momento);
}

