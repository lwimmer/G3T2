package at.ac.tuwien.infosys.aic2016.g3t2.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception describes the fact that a requested item has not been found.
 */

@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="Item missing")
public class ItemMissingException extends Exception {
	private static final long serialVersionUID = 1L;
}
