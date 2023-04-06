package org.opennms.horizon.email.grpc;

import com.google.protobuf.Empty;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.email.exception.EmailException;
import org.opennms.horizon.email.proto.EmailServiceGrpc;
import org.opennms.horizon.email.service.EmailService;
import org.springframework.stereotype.Component;
import org.opennms.horizon.email.proto.Email;

@Component
@RequiredArgsConstructor
public class EmailGrpcService extends EmailServiceGrpc.EmailServiceImplBase {

    private final EmailService emailService;

    @Override
    public void sendEmail(Email email, StreamObserver<Empty> responseObserver) {
        try {
            emailService.sendEmail(email);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (EmailException e) {
            responseObserver.onError(e);
        }
    }
}
