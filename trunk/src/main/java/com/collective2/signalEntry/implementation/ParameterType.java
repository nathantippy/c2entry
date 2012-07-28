/**
 * This notice shall not be removed.
 * See the "LICENSE.txt" file found in the root folder
 * for the full license governing this code.
 * Nathan Tippy  7/23/12
 */
package com.collective2.signalEntry.implementation;

import com.collective2.signalEntry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public enum ParameterType {

    StringType(String.class) {
        @Override
        public Object parse(String stringValue) {
            return stringValue;
        }
    },
    IntegerType(Integer.class) {
        @Override
        public Object parse(String stringValue) {
            return Integer.valueOf(stringValue);
        }
    },
    CommandType(Command.class) {
        @Override
        public Object parse(String stringValue) {
            return lookupEnum(stringValue, Command.values());
        }
    },
    IntDotStringType(DotString.class) {
        @Override
        public Object parse(String stringValue) {

            String[] parts = stringValue.split("\\.");
            int i = parts.length;
            Integer[] data = new Integer[i];
            while (--i>=0) {
                data[i]=Integer.valueOf(parts[i]);
            }
            return new DotString<Integer>(data);
        }
    },
    ActionForStockType(ActionForStock.class) {
        @Override
        public Object parse(String stringValue) {
            return lookupEnum(stringValue, ActionForStock.values());
        }
    },
    ActionForNonStockType(ActionForNonStock.class) {
        @Override
        public Object parse(String stringValue) {
            return lookupEnum(stringValue, ActionForNonStock.values());
        }
    },
    DurationType(Duration.class) {
        @Override
        public Object parse(String stringValue) {
            return lookupEnum(stringValue, Duration.values());
        }
    },
    NumberType(Number.class) {
        @Override
        public Object parse(String stringValue) {
            return new BigDecimal(stringValue);
        }
    },
    RelativeNumberType(RelativeNumber.class) {
        @Override
        public Object parse(String stringValue) {
            return new RelativeNumber(stringValue);
        }
    },
    RelatedType(Related.class) {
        @Override
        public Object parse(String stringValue) {
            return lookupEnum(stringValue, Related.values());
        }
    },
    InstrumentType(Instrument.class) {
        @Override
        public Object parse(String stringValue) {
            return lookupEnum(stringValue, Instrument.values());
        }
    },
    DigitsFixed14(String.class) {
        @Override
        public Object parse(String stringValue) {
            if (stringValue.length()!=14) {
                throw new ArrayIndexOutOfBoundsException("Must be exactly 14 chars long.");
            }
            int i = stringValue.length();
            while(--i>=0) {
                if (stringValue.charAt(i)<'0' || stringValue.charAt(i)>'9') {
                    throw new C2ServiceException("Unable to parse should be 14 digits but found: "+stringValue,false);
                }
            }
            return stringValue;
        }
        @Override
        public void validate(Object value) {
            super.validate(value);
            String temp = value.toString();
            int i = temp.length();
            while(--i>=0) {
                if (temp.charAt(i)<'0' || temp.charAt(i)>'9') {
                    throw new C2ServiceException("Unable to parse should be 14 digits but found: "+temp,false);
                }
            }
        }
    };

    private static Object lookupEnum(String stringValue, Enum[] values) {
        for(Enum e:values) {
            if (e.toString().equalsIgnoreCase(stringValue)) {
                return e;
            }
        }
        throw new C2ServiceException("Unable to find:"+stringValue+" in "+values.getClass(),false);
    }

    private final Class clazz;

    private static final Logger logger = LoggerFactory.getLogger(ParameterType.class);

    ParameterType(Class clazz) {
        this.clazz = clazz;
    }

    public abstract Object parse(String stringValue);

    public void validate(Object value) {
        if (value == null) {
            String message = "Null value for "+this.name()+" is not supported.";
            logger.error(message);
            throw new C2ServiceException(message, false);
        }
        if (!clazz.isInstance(value)) {
            String message = "Invalid value '" + value + "' for " + this.name();
            logger.error(message);
            throw new C2ServiceException(message, false);
        }
    };

    public boolean isClass(Class clazz) {
        return clazz.isAssignableFrom(this.clazz);
    }
}
