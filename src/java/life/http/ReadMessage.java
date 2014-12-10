package life.http;

import life.Account;
import life.Appendix;
import life.LifeCoin;
import life.Transaction;
import life.crypto.Crypto;
import life.util.Convert;
import life.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static life.http.JSONResponses.INCORRECT_TRANSACTION;
import static life.http.JSONResponses.MISSING_TRANSACTION;
import static life.http.JSONResponses.NO_MESSAGE;
import static life.http.JSONResponses.UNKNOWN_TRANSACTION;

public final class ReadMessage extends APIServlet.APIRequestHandler {

    static final ReadMessage instance = new ReadMessage();

    private ReadMessage() {
        super(new APITag[] {APITag.MESSAGES}, "transaction", "secretPhrase");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        String transactionIdString = Convert.emptyToNull(req.getParameter("transaction"));
        if (transactionIdString == null) {
            return MISSING_TRANSACTION;
        }

        Transaction transaction;
        try {
            transaction = LifeCoin.getBlockchain().getTransaction(Convert.parseUnsignedLong(transactionIdString));
            if (transaction == null) {
                return UNKNOWN_TRANSACTION;
            }
        } catch (RuntimeException e) {
            return INCORRECT_TRANSACTION;
        }

        JSONObject response = new JSONObject();
        Account senderAccount = Account.getAccount(transaction.getSenderId());
        Appendix.Message message = transaction.getMessage();
        Appendix.EncryptedMessage encryptedMessage = transaction.getEncryptedMessage();
        Appendix.EncryptToSelfMessage encryptToSelfMessage = transaction.getEncryptToSelfMessage();
        if (message == null && encryptedMessage == null && encryptToSelfMessage == null) {
            return NO_MESSAGE;
        }
        if (message != null) {
            response.put("message", message.isText() ? Convert.toString(message.getMessage()) : Convert.toHexString(message.getMessage()));
        }
        String secretPhrase = Convert.emptyToNull(req.getParameter("secretPhrase"));
        if (secretPhrase != null) {
            if (encryptedMessage != null) {
                long readerAccountId = Account.getId(Crypto.getPublicKey(secretPhrase));
                Account account = senderAccount.getId() == readerAccountId ? Account.getAccount(transaction.getRecipientId()) : senderAccount;
                if (account != null) {
                    try {
                        byte[] decrypted = account.decryptFrom(encryptedMessage.getEncryptedData(), secretPhrase);
                        response.put("decryptedMessage", encryptedMessage.isText() ? Convert.toString(decrypted) : Convert.toHexString(decrypted));
                    } catch (RuntimeException e) {
                        Logger.logDebugMessage("Decryption of message to recipient failed: " + e.toString());
                    }
                }
            }
            if (encryptToSelfMessage != null) {
                Account account = Account.getAccount(Crypto.getPublicKey(secretPhrase));
                if (account != null) {
                    try {
                        byte[] decrypted = account.decryptFrom(encryptToSelfMessage.getEncryptedData(), secretPhrase);
                        response.put("decryptedMessageToSelf", encryptToSelfMessage.isText() ? Convert.toString(decrypted) : Convert.toHexString(decrypted));
                    } catch (RuntimeException e) {
                        Logger.logDebugMessage("Decryption of message to self failed: " + e.toString());
                    }
                }
            }
        }
        return response;
    }

}