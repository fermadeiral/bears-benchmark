package com.cmpl.web.core.factory;

import com.cmpl.web.core.common.message.WebMessageSource;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultBaseFactoryTest {

  @Mock
  private WebMessageSource messageSource;

  @InjectMocks
  @Spy
  private DefaultBaseDisplayFactory displayFactory;

  @Test
  public void testGetI18nValue() throws Exception {
    String value = "value";
    String key = "key";

    BDDMockito.given(messageSource.getI18n(BDDMockito.eq(key), BDDMockito.eq(Locale.FRANCE)))
        .willReturn(value);

    String result = displayFactory.getI18nValue(key, Locale.FRANCE);

    Assert.assertEquals(value, result);
  }
}
