package org.hexaware.userservice.services;

import java.util.UUID;

public interface WalletService {

    public void processWalletTransfer(UUID payerUserId, UUID payeeUserId, Double amount);
}
