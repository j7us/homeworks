package ru.otus.bank.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.otus.bank.dao.AccountDao;
import ru.otus.bank.entity.Account;
import ru.otus.bank.entity.Agreement;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class PaymentProcessorImplWithSpyTest {

    @Mock
    AccountDao accountDao;

    @Spy
    @InjectMocks
    AccountServiceImpl accountService;

    @InjectMocks
    PaymentProcessorImpl paymentProcessor;

    Agreement sourceAgreement;
    Agreement destinationAgreement;
    Account sourceAccount;
    Account destinationAccount;

    @BeforeEach
    public void init() {
        paymentProcessor = new PaymentProcessorImpl(accountService);

        sourceAgreement = new Agreement();
        sourceAgreement.setId(1L);

        destinationAgreement = new Agreement();
        destinationAgreement.setId(2L);

        sourceAccount = new Account();
        sourceAccount.setAmount(BigDecimal.TEN);
        sourceAccount.setType(0);
        sourceAccount.setId(10L);

        destinationAccount = new Account();
        destinationAccount.setAmount(BigDecimal.ZERO);
        destinationAccount.setType(0);
        destinationAccount.setId(20L);
    }

    @Test
    public void testTransfer() {
        doReturn(List.of(sourceAccount)).when(accountService).getAccounts(argThat(new ArgumentMatcher<Agreement>() {
            @Override
            public boolean matches(Agreement argument) {
                return argument != null && argument.getId() == 1L;
            }
        }));

        doReturn(List.of(destinationAccount)).when(accountService).getAccounts(argThat(new ArgumentMatcher<Agreement>() {
            @Override
            public boolean matches(Agreement argument) {
                return argument != null && argument.getId() == 2L;
            }
        }));

        when(accountDao.findById(10L)).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(20L)).thenReturn(Optional.of(destinationAccount));
//        when(accountDao.findById(30L)).thenReturn(Optional.of(destinationAccount));

        paymentProcessor.makeTransfer(sourceAgreement, destinationAgreement,
                0, 0, BigDecimal.ONE);

        assertEquals(new BigDecimal(9), sourceAccount.getAmount());
        assertEquals(BigDecimal.ONE, destinationAccount.getAmount());

    }

    @Test
    public void testTransferWithComission() {
        doReturn(List.of(sourceAccount)).when(accountService).getAccounts(argThat(new ArgumentMatcher<Agreement>() {
            @Override
            public boolean matches(Agreement argument) {
                return argument != null && argument.getId() == 1L;
            }
        }));

        doReturn(List.of(destinationAccount)).when(accountService).getAccounts(argThat(new ArgumentMatcher<Agreement>() {
            @Override
            public boolean matches(Agreement argument) {
                return argument != null && argument.getId() == 2L;
            }
        }));

        when(accountDao.findById(10L)).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(20L)).thenReturn(Optional.of(destinationAccount));

        paymentProcessor.makeTransferWithComission(sourceAgreement, destinationAgreement,
                0, 0, BigDecimal.TEN, BigDecimal.valueOf(0.1));

        assertEquals(BigDecimal.valueOf(1.0), sourceAccount.getAmount());
        assertEquals(BigDecimal.TEN, destinationAccount.getAmount());
    }

}
