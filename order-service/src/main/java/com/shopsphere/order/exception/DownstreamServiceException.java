package com.shopsphere.order.exception;

public class DownstreamServiceException extends RuntimeException {

    private final int status;
    private final String service;

    public DownstreamServiceException(
            String service,
            int status,
            String message) {
        super(message);
        this.service = service;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public String getService() {
        return service;
    }
}
