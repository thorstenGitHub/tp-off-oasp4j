package io.oasp.gastronomy.restaurant.general.common.api.exception;

import io.oasp.gastronomy.restaurant.general.common.api.NlsBundleApplicationRoot;

/**
 * Thrown when an operation is requested that requires a user to be logged in, but no such user exists.
 *
 * @author drihoet
 */
public class NoActiveUserException extends ApplicationBusinessException {

  /** UID for serialization. */
  private static final long serialVersionUID = 1L;

  /**
   * The constructor.
   */
  public NoActiveUserException() {

    this(null);
  }

  /**
   * The constructor.
   * 
   * @param cause The root cause of this exception.
   */
  public NoActiveUserException(Throwable cause) {

    super(cause, createBundle(NlsBundleApplicationRoot.class).errorNoActiveUser());
  }

}
