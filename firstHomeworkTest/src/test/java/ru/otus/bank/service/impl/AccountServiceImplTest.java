package ru.otus.bank.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.bank.dao.AccountDao;
import ru.otus.bank.entity.Account;
import ru.otus.bank.entity.Agreement;
import ru.otus.bank.service.exception.AccountException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    @Mock
    AccountDao accountDao;

    @InjectMocks
    AccountServiceImpl accountServiceImpl;

    @Mock
    Agreement agreementMock;

    @Test
    void testAddAccount() {
        Account testAccount = new Account();
        testAccount.setAgreementId(1L);
        testAccount.setNumber("1234");
        testAccount.setType(0);
        testAccount.setAmount(BigDecimal.ONE);

        when(agreementMock.getId()).thenReturn(1L);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

        when(accountDao.save(accountCaptor.capture())).thenReturn(testAccount);

        accountServiceImpl.addAccount(agreementMock, "1234", 0, BigDecimal.ONE);

        assertEquals(testAccount.getAgreementId(), accountCaptor.getValue().getAgreementId());
        assertEquals(testAccount.getNumber(), accountCaptor.getValue().getNumber());
        assertEquals(testAccount.getType(), accountCaptor.getValue().getType());
        assertEquals(testAccount.getAmount(), accountCaptor.getValue().getAmount());
    }

    @Test
    void testGetAccounts() {
        doAnswer(invocation -> {
            long agreementId = invocation.getArgument(0);
            Account account = new Account();
            account.setAgreementId(agreementId);
            return List.of(account);
        }).when(accountDao).findByAgreementId(anyLong());

        when(agreementMock.getId()).thenReturn(1L);

        List<Account> resultAccounts = accountServiceImpl.getAccounts(agreementMock);

        assertEquals(1, resultAccounts.size());
        assertEquals(1L, resultAccounts.get(0).getAgreementId());
    }

    @Test
    void testCharge() {
        Account testAccount = new Account();
        testAccount.setAmount(BigDecimal.TEN);

        when(accountDao.findById(anyLong())).thenReturn(Optional.of(testAccount));

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        when(accountDao.save(accountCaptor.capture())).thenReturn(testAccount);

        accountServiceImpl.charge(2L, BigDecimal.ONE);

        assertEquals(BigDecimal.valueOf(9), accountCaptor.getValue().getAmount());
    }

    @Test
    void testNoSourceAccountCharge() {
        when(accountDao.findById(anyLong())).thenReturn(Optional.empty());

        AccountException accountException = assertThrows(AccountException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                accountServiceImpl.charge(1L, BigDecimal.ONE);
            }
        });

        assertEquals("No source account", accountException.getLocalizedMessage());
    }

    @Test
    void testTransfer() {
        Account sourceAccount = new Account();
        sourceAccount.setAmount(new BigDecimal(100));

        Account destinationAccount = new Account();
        destinationAccount.setAmount(new BigDecimal(10));

        when(accountDao.findById(eq(1L))).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(eq(2L))).thenReturn(Optional.of(destinationAccount));

        accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));

        assertEquals(new BigDecimal(90), sourceAccount.getAmount());
        assertEquals(new BigDecimal(20), destinationAccount.getAmount());
    }

    @Test
    void testSourceNotFound() {
        when(accountDao.findById(any())).thenReturn(Optional.empty());

        AccountException result = assertThrows(AccountException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));
            }
        });
        assertEquals("No source account", result.getLocalizedMessage());
    }


    @Test
    void testTransferWithVerify() {
        Account sourceAccount = new Account();
        sourceAccount.setAmount(new BigDecimal(100));
        sourceAccount.setId(1L);

        Account destinationAccount = new Account();
        destinationAccount.setAmount(new BigDecimal(10));
        destinationAccount.setId(2L);

        when(accountDao.findById(eq(1L))).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(eq(2L))).thenReturn(Optional.of(destinationAccount));

        ArgumentMatcher<Account> sourceMatcher =
                argument -> argument.getId().equals(1L) && argument.getAmount().equals(new BigDecimal(90));

        ArgumentMatcher<Account> destinationMatcher =
                argument -> argument.getId().equals(2L) && argument.getAmount().equals(new BigDecimal(20));

        accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));

        verify(accountDao).save(argThat(sourceMatcher));
        verify(accountDao).save(argThat(destinationMatcher));
        }
}
