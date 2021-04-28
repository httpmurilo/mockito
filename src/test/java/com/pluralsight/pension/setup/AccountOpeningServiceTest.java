package com.pluralsight.pension.setup;

import com.pluralsight.pension.AccountRepository;
import com.pluralsight.pension.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import org.mockito.*;
import sun.util.resources.LocaleData;

class AccountOpeningServiceTest {

    private AccountOpeningService underTest;
    private BackgroundCheckService backgroundCheckService = mock(BackgroundCheckService.class);
    private ReferenceIdsManager referenceIdsManager = mock(ReferenceIdsManager.class);
    private AccountRepository accountRepository = mock(AccountRepository.class);
    private AccountOpeningEventPublisher eventPublisher = mock(AccountOpeningEventPublisher.class);
    private BackgroundCheckResults backgroundCheckResults = mock(BackgroundCheckResults.class);
    private static final String FIRST_NAME = "Murilo";
    private static final String LAST_NAME = "Eduardo";
    private static final String TAX_ID = "222";
    private static final LocalDate DOB = LocalDate.of(1990,1,1);

    @BeforeEach
    void setUp() {
        underTest = new AccountOpeningService(backgroundCheckService,referenceIdsManager,accountRepository, eventPublisher);
    }

    @Test
    public void shoudOpenAccount() throws IOException {
        when(backgroundCheckService.confirm(FIRST_NAME,LAST_NAME,"123XYZ9", DOB))
                .thenReturn(new BackgroundCheckResults("something_not_unacceptable",100));
        when(referenceIdsManager.obtainId(eq(FIRST_NAME),anyString(),eq(LAST_NAME),eq(TAX_ID),eq(DOB)))
                .thenReturn("some_id");
        final AccountOpeningStatus accountOpeningStatus = underTest.openAccount(FIRST_NAME,LAST_NAME,"123XZY",DOB);
        assertEquals(AccountOpeningStatus.DECLINED, accountOpeningStatus);
    }

    @Test
    public void shouldDeclineAccountIfUnnaceptableRiskProfileBackgroundCheckResponseReceived() throws  IOException{
        Mockito.when(backgroundCheckService.confirm(FIRST_NAME,LAST_NAME,"123XZY",DOB)
        ).thenReturn(new BackgroundCheckResults("UNACEPPTABLE_RISK_PROFILE",0));
        final AccountOpeningStatus accountOpeningStatus = underTest.openAccount(FIRST_NAME,LAST_NAME,"123XZY",DOB);
        assertEquals(AccountOpeningStatus.DECLINED, accountOpeningStatus);
    }

    @Test
    public void shouldDeclineAccountIfNullBackgroudCheckResponseReceived() throws IOException {
        Mockito.when(backgroundCheckService.confirm(FIRST_NAME,LAST_NAME,"123XZY",DOB)
        ).thenReturn(null);
        final AccountOpeningStatus accountOpeningStatus = underTest.openAccount(FIRST_NAME,LAST_NAME,"123XZY",DOB);
        assertEquals(AccountOpeningStatus.DECLINED, accountOpeningStatus);
    }

    @Test
    public void shouldThrowIfBackgroundCHecksServiceThrows() throws IOException{
        when(backgroundCheckService.confirm(FIRST_NAME,LAST_NAME,TAX_ID,DOB))
                .thenThrow(new IOException());
        assertThrows(IOException.class, () -> underTest.openAccount(FIRST_NAME,LAST_NAME,TAX_ID,DOB));
    }
    @Test
    public void shouldThrowIfReferenceIdsManagerThrows() throws IOException{
        when(backgroundCheckService.confirm(FIRST_NAME,LAST_NAME,"123XYZ9", DOB))
                .thenReturn(new BackgroundCheckResults("something_not_unacceptable",100));
        when(referenceIdsManager.obtainId(eq(FIRST_NAME),anyString(),eq(LAST_NAME),eq(TAX_ID),eq(DOB)))
                .thenReturn("some_id");
        assertThrows(RuntimeException.class,() -> underTest.openAccount(FIRST_NAME,LAST_NAME,TAX_ID,DOB));
    }

    @Test
    public void shouldThrowIfAccountRepositoryThrows() throws IOException {
        when(backgroundCheckService.confirm(FIRST_NAME,LAST_NAME,"123XYZ9", DOB))
                .thenReturn(new BackgroundCheckResults("something_not_unacceptable",100));
        when(referenceIdsManager.obtainId(eq(FIRST_NAME),anyString(),eq(LAST_NAME),eq(TAX_ID),eq(DOB)))
                .thenReturn("someID");
        when(accountRepository.save("someID", FIRST_NAME,LAST_NAME,TAX_ID,DOB, backgroundCheckResults))
        .thenThrow(new RuntimeException());
        assertThrows(RuntimeException.class, () -> underTest.openAccount(FIRST_NAME,LAST_NAME,TAX_ID,DOB));
    }
}