package exception;

import java.io.IOException;

public class CaminhoJaExistenteException extends IOException {
    public CaminhoJaExistenteException(String message) {
        super(message);
    }
}
