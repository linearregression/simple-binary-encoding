/* -*- mode: java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*- */
/*
 * Copyright 2013 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.PrimitiveValue;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static uk.co.real_logic.sbe.xml.XmlSchemaParser.handleError;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.handleWarning;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.checkForValidName;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.getAttributeValue;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.getAttributeValueOrNull;

/**
 * SBE enumType
 */
public class EnumType extends Type
{
    private final PrimitiveType encodingType;
    private final PrimitiveValue nullValue;
    private final Map<PrimitiveValue, ValidValue> validValueByPrimitiveValueMap = new HashMap<>();
    private final Map<String, ValidValue> validValueByNameMap = new HashMap<>();

    /**
     * Construct a new enumType from XML Schema.
     *
     * @param node from the XML Schema Parsing
     */
    public EnumType(final Node node)
        throws XPathExpressionException, IllegalArgumentException
    {
        super(node);

        encodingType = PrimitiveType.get(getAttributeValue(node, "encodingType"));
        if (encodingType != PrimitiveType.CHAR && encodingType != PrimitiveType.UINT8)
        {
            throw new IllegalArgumentException("illegal encodingType " + encodingType);
        }

        String nullValueStr = getAttributeValueOrNull(node, "nullVal");
        if (nullValueStr != null)
        {
            if (presence() != Presence.OPTIONAL)
            {
                handleError(node, "nullVal set, but presence is not optional");
                nullValue = null;
            }
            else
            {
                nullValue = PrimitiveValue.parse(nullValueStr, encodingType);
            }
        }
        else
        {
            nullValue = null;
        }

        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList list = (NodeList)xPath.compile("validValue").evaluate(node, XPathConstants.NODESET);

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            ValidValue v = new ValidValue(list.item(i), encodingType);

            if (validValueByPrimitiveValueMap.get(v.primitiveValue()) != null)
            {
                handleWarning(node, "validValue already exists for value: " + v.primitiveValue());
            }

            if (validValueByNameMap.get(v.name()) != null)
            {
                handleWarning(node, "validValue already exists for name: " + v.name());
            }

            validValueByPrimitiveValueMap.put(v.primitiveValue(), v);
            validValueByNameMap.put(v.name(), v);
        }
    }

    public PrimitiveType encodingType()
    {
        return encodingType;
    }

    /**
     * The size (in octets) of the encodingType
     *
     * @return size of the encodingType
     */
    public int size()
    {
        return encodingType.size();
    }

    /**
     * Get the {@link ValidValue} represented by a {@link PrimitiveValue}.
     *
     * @param value to get.
     * @return the {@link ValidValue} represented by a {@link PrimitiveValue} or null.
     */
    public ValidValue getValidValue(final PrimitiveValue value)
    {
        return validValueByPrimitiveValueMap.get(value);
    }

    /**
     * Get the {@link ValidValue} represented by a string name.
     *
     * @param name to get.
     * @return the {@link ValidValue} represented by a string name or null.
     */
    public ValidValue getValidValue(final String name)
    {
        return validValueByNameMap.get(name);
    }

    /**
     * The nullVal of the type
     *
     * @return value of the nullVal
     */
    public PrimitiveValue nullValue()
    {
        return nullValue;
    }

    /**
     * The collection of valid values for this enumeration.
     *
     * @return the collection of valid values for this enumeration.
     */
    public Collection<ValidValue> validValues()
    {
        return validValueByNameMap.values();
    }

    /**
     * Class to hold valid values for EnumType
     */
    public static class ValidValue
    {
        private final String name;
        private final String description;
        private final PrimitiveValue value;
        private final int sinceVersion;

        /**
         * Construct a ValidValue given the XML node and the encodingType.
         *
         * @param node         that contains the validValue
         * @param encodingType for the enum
         */
        public ValidValue(final Node node, final PrimitiveType encodingType)
        {
            name = getAttributeValue(node, "name");
            description = getAttributeValueOrNull(node, "description");
            value = PrimitiveValue.parse(node.getFirstChild().getNodeValue(), encodingType);
            sinceVersion = Integer.parseInt(getAttributeValue(node, "sinceVersion", "0"));

            checkForValidName(node, name);
        }

        /**
         * {@link PrimitiveType} for the {@link ValidValue}.
         *
         * @return {@link PrimitiveType} for the {@link ValidValue}.
         */
        public PrimitiveValue primitiveValue()
        {
            return value;
        }

        /**
         * The name of the {@link ValidValue}.
         *
         * @return the name of the {@link ValidValue}
         */
        public String name()
        {
            return name;
        }

        /**
         * The description of the {@link ValidValue}.
         *
         * @return the description of the {@link ValidValue}.
         */
        public String description()
        {
            return description;
        }

        /**
         * The sinceVersion value of the {@link ValidValue}
         *
         * @return the sinceVersion value of the {@link ValidValue}
         */
        public int sinceVersion()
        {
            return sinceVersion;
        }
    }
}
