package info.kgeorgiy.ja.malko.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

/**
 * Wrapper for {@link ImplerException}
 */
public class ImplementorException extends ImplerException {

  /**
   * Constructor of {@link ImplementorException} with specified message.
   *
   * @param message exception message
   */
  public ImplementorException(String message) {
    super(message);
  }

  /**
   * Constructor of {@link ImplementorException} with specified {@code message} and {@code cause}.
   *
   * @param message exception message
   * @param cause   exception cause
   */
  public ImplementorException(String message, Throwable cause) {
    super(message, cause);
  }
}
