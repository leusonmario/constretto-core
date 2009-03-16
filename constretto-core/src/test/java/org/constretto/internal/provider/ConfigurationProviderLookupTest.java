/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constretto.internal.provider;

import org.constretto.ConstrettoBuilder;
import org.constretto.ConstrettoConfiguration;
import org.constretto.exception.ConstrettoException;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * @author <a href="mailto:kristoffer.moum@arktekk.no">Kristoffer Moum</a>
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public class ConfigurationProviderLookupTest {

    @After
    public void cleanup() {
        System.clearProperty("key1");
    }

    @Test
    public void simpleLookupForExistingKeyNotUsingDefaultValue() {
        ConstrettoConfiguration config = prepareTests();
        assertEquals("key1-value", config.evaluateToString("key1"));
    }

    @Test
    public void simpleLookupForExistingKeyUsingDefaultValue() {
        ConstrettoConfiguration config = prepareTests();
        assertEquals("key1-value", config.evaluateTo("key1", "default-key-value"));
    }

    @Test
    public void simpleLookupForExistingKeyWithOverridenValue() {
        ConstrettoConfiguration config = prepareTests();
        assertEquals("in-the-second-file-in-the-list", config.evaluateToString("i-am"));
        assertEquals("key1-value", config.evaluateToString("key1"));
        System.setProperty("key1", "key1-value-in-system-properties");
        config = prepareTests();
        assertEquals("key1-value-in-system-properties", config.evaluateToString("key1"));

    }

    @Test(expected = ConstrettoException.class)
    public void simpleLookupForMissingKeyNotUsingDefaultValue() {
        ConstrettoConfiguration constrettoConfiguration = prepareTests();
        constrettoConfiguration.evaluateTo(Integer.class, "missing.key");
    }

    @Test
    public void simpleLookupForMissingKeyUsingDefaultValue() {
        ConstrettoConfiguration constrettoConfiguration = prepareTests();
        Integer value = constrettoConfiguration.evaluateTo("missing.key", Integer.MIN_VALUE);
        assertEquals(new Integer(Integer.MIN_VALUE), value);
    }

    @Test(expected = ConstrettoException.class)
    public void simpleTaggedLookupForKeyNotInCurrentTagAndNotInDefaultTag() {
        ConstrettoConfiguration constrettoConfiguration = prepareTests("production");
        assertEquals("I only exist in development", constrettoConfiguration.evaluateToString("ionlyexistindevelopment"));
        constrettoConfiguration = prepareTests("production");
        constrettoConfiguration.evaluateToString("ionlyexistindevelopment");
    }

    @Test
    public void simpleTaggedLookupForKeyNotInCurrentTagButExistsInDefaultTag() {
        ConstrettoConfiguration constrettoConfiguration = prepareTests("production");
        assertEquals("key1-value", constrettoConfiguration.evaluateToString("key1"));
    }

    @Test(expected = ConstrettoException.class)
    public void multiTaggedLookupForKeyNotInAnyCurrentTagsAndNotInDefaultTag() {
        ConstrettoConfiguration constrettoConfiguration = prepareTests("production", "test", "some-tag");
        constrettoConfiguration.evaluateToString("ionlyexistindevelopment");
    }

    @Test
    public void multiTaggedLookupForKeyNotInAnyCurrentTagsButExistsInDefaultTag() {
        ConstrettoConfiguration constrettoConfiguration = prepareTests("productiontest", "test", "some-tag");
        assertEquals("http://webservice", constrettoConfiguration.evaluateToString("webservices-base-url"));
    }

    @Test
    public void multiTaggedLookupForKeyInOneOfTheCurrentTagsButNotInDefaultTag() {
        ConstrettoConfiguration constrettoConfiguration = prepareTests("production", "development", "some-tag");
        assertEquals("I only exist in development", constrettoConfiguration.evaluateToString("ionlyexistindevelopment"));
    }

    @Test
    public void multiTaggedLookupForKeyInSeveralOfTheCurrentTagsAndInDefaultTag() {
        ConstrettoConfiguration constrettoConfiguration = prepareTests("production", "development");
        assertEquals("http://production.webservice", constrettoConfiguration.evaluateToString("webservices-base-url"));
        constrettoConfiguration = prepareTests("development", "production");
        assertEquals("http://development.webservice", constrettoConfiguration.evaluateToString("webservices-base-url"));
        constrettoConfiguration = prepareTests();
        assertEquals("http://webservice", constrettoConfiguration.evaluateToString("webservices-base-url"));
    }

    public void simpleLookupForKeyContainingReferencesToOtherKeys() {
    }

    public void simpleLookupForKeyContainingDirectCircularReferencesToOtherKeys() {
    }

    public void simpleLookupForKeyContainingTransitiveCircularReferencesToOtherKeys() {
    }

    public void simpleTaggedLookupForKeyContainingReferencesToOtherKeys() {
    }

    public void multiTaggedLookupForKeyContainingReferencesToOtherKeys() {
    }

    public ConstrettoConfiguration prepareTests(String... tags) {
        ConstrettoBuilder constrettoBuilder = new ConstrettoBuilder();
        constrettoBuilder
                .createPropertiesStore()
                .addResource(new ClassPathResource(("org/constretto/internal/provider/helper/provider-test.properties")))
                .addResource(new ClassPathResource(("org/constretto/internal/provider/helper/provider-test-overloaded.properties")))
                .done()
                .createSystemPropertiesStore();
        for (String tag : tags) {
            constrettoBuilder.addCurrentTag(tag);
        }
        return constrettoBuilder.getConfiguration();
    }

}
