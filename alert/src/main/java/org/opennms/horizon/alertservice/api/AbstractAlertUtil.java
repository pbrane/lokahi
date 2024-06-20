/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.horizon.alertservice.api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opennms.horizon.alertservice.resolver.AlertTemplate;
import org.opennms.horizon.alertservice.resolver.ExpandableParameterResolver;
import org.opennms.horizon.alertservice.resolver.ExpandableParameterResolverRegistry;
import org.opennms.horizon.events.proto.Event;
import org.opennms.horizon.events.proto.EventParameter;
import org.opennms.horizon.events.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractAlertUtil implements AlertUtilService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractAlertUtil.class);

    public static final String TAG_UEI = "uei";
    /**
     * The Event ID xml
     */
    public static final String TAG_EVENT_DB_ID = "eventid";

    /**
     * The event source xml tag
     */
    public static final String TAG_SOURCE = "source";

    /**
     * The event descr xml tag
     */
    public static final String TAG_DESCR = "descr";

    /**
     * The event logmsg xml tag
     */
    public static final String TAG_LOGMSG = "logmsg";

    /**
     * The event time xml tag
     */
    public static final String TAG_TIME = "time";

    /**
     * The event time xml tag, short format
     */
    public static final String TAG_SHORT_TIME = "shorttime";

    /**
     * The event dpname xml tag
     */
    public static final String TAG_DPNAME = "dpname";

    /**
     * The event nodeid xml tag
     */
    public static final String TAG_NODEID = "nodeid";

    /**
     * The event nodelabel xml tag
     */
    public static final String TAG_NODELABEL = "nodelabel";

    /**
     * The event nodelocation xml tag
     */
    public static final String TAG_NODELOCATION = "nodelocation";

    /**
     * The event host xml tag
     */
    public static final String TAG_HOST = "host";

    /**
     * The event interface xml tag
     */
    public static final String TAG_INTERFACE = "interface";

    /**
     * The foreignsource for the event's nodeid xml tag
     */
    public static final String TAG_FOREIGNSOURCE = "foreignsource";

    /**
     * The foreignid for the event's nodeid xml tag
     */
    public static final String TAG_FOREIGNID = "foreignid";

    /**
     * The event ifindex xml tag
     */
    public static final String TAG_IFINDEX = "ifindex";

    /**
     * The reverse DNS lookup of the interface
     */
    public static final String TAG_INTERFACE_RESOLVE = "interfaceresolve";

    /**
     * The primary interface
     */
    public static final String TAG_PRIMARY_INTERFACE_ADDRESS = "primaryinterface";

    /**
     * The reverse DNS lookup of the interface
     */
    public static final String TAG_IFALIAS = "ifalias";

    /**
     * The event snmp id xml tag
     */
    public static final String TAG_SNMP_ID = "id";

    /**
     * The event snmp trapoid xml tag
     */
    public static final String TAG_SNMP_TRAP_OID = "trapoid";

    /**
     * The SNMP xml tag
     */
    public static final String TAG_SNMP = "snmp";

    /**
     * The event snmp idtext xml tag
     */
    public static final String TAG_SNMP_IDTEXT = "idtext";

    /**
     * The event snmp version xml tag
     */
    public static final String TAG_SNMP_VERSION = "version";

    /**
     * The event snmp specific xml tag
     */
    public static final String TAG_SNMP_SPECIFIC = "specific";

    /**
     * The event snmp generic xml tag
     */
    public static final String TAG_SNMP_GENERIC = "generic";

    /**
     * The event snmp community xml tag
     */
    public static final String TAG_SNMP_COMMUNITY = "community";

    /**
     * The event snmp host xml tag
     */
    public static final String TAG_SNMPHOST = "snmphost";

    /**
     * The event service xml tag
     */
    public static final String TAG_SERVICE = "service";

    /**
     * The event severity xml tag
     */
    public static final String TAG_SEVERITY = "severity";

    /**
     * The event operinstruct xml tag
     */
    public static final String TAG_OPERINSTR = "operinstruct";

    /**
     * The event mouseovertext xml tag
     */
    public static final String TAG_MOUSEOVERTEXT = "mouseovertext";

    public static final String TAG_TTICKET_ID = "tticketid";

    /**
     * The string that starts the expansion for an asset field - used to lookup values
     * of asset fields by their names
     */
    public static final String ASSET_BEGIN = "asset[";

    /**
     * The string that ends the expansion of a parm
     */
    public static final String ASSET_END_SUFFIX = "]";

    /**
     * The string that should be expanded to a list of all parm names
     */
    public static final String PARMS_NAMES = "parm[names-all]";

    /**
     * The string that should be expanded to a list of all parm values
     */
    public static final String PARMS_VALUES = "parm[values-all]";

    /**
     * The string that should be expanded to a list of all parms
     */
    public static final String PARMS_ALL = "parm[all]";

    /**
     * The string that starts the expansion for a parm - used to lookup values
     * of parameters by their names
     */
    public static final String PARM_BEGIN = "parm[";

    /**
     * Pattern used to match and parse 'parm' tokens.
     */
    public static final Pattern PARM_REGEX = Pattern.compile("^parm\\[(.*)\\]$");

    /**
     * The length of PARM_BEGIN
     */
    public static final int PARM_BEGIN_LENGTH = 5;

    /**
     * The string that should be expanded to the number of parms
     */
    public static final String NUM_PARMS_STR = "parm[##]";

    /**
     * The string that starts a parm number - used to lookup values of
     * parameters by their position
     */
    public static final String PARM_NUM_PREFIX = "parm[#";

    /**
     * The length of PARM_NUM_PREFIX
     */
    public static final int PARM_NUM_PREFIX_LENGTH = 6;

    /**
     * The string that starts a request for the name of a numbered parm
     */
    public static final String PARM_NAME_NUMBERED_PREFIX = "parm[name-#";

    /**
     * The length of PARM_NAME_NUMBERED_PREFIX
     */
    public static final int PARM_NAME_NUMBERED_PREFIX_LENGTH = 11;

    /**
     * The string that ends the expansion of a parm
     */
    public static final String PARM_END_SUFFIX = "]";

    /**
     * For expansion of the '%parms[all]%' - the parm name and value are added
     * as delimiter separated list of ' <parmName>= <value>' strings
     */
    public static final char NAME_VAL_DELIM = '=';

    /**
     */
    public static final char SPACE_DELIM = ' ';

    /**
     * The values and the corresponding attributes of an element are added
     * delimited by ATTRIB_DELIM
     */
    public static final char ATTRIB_DELIM = ',';

    /**
     * Substitute the actual percent sign
     */
    public static final String TAG_PERCENT_SIGN = "pctsign";

    /**
     * The string that starts the expansion for a hardware field - used to lookup values
     * of hardware attributes by their index|name
     */
    public static final String HARDWARE_BEGIN = "hardware[";

    /**
     * The string that ends the expansion of a hardware
     */
    public static final String HARDWARE_END_SUFFIX = "]";

    /**
     * <P>
     * This method is used to escape required values from strings that may
     * contain those values. If the passed string contains the passed value then
     * the character is reformatted into its <EM>%dd</EM> format.
     * </P>
     *
     * @param inStr
     *            string that might contain the delimiter
     * @param delimchar
     *            delimiter to escape
     * @return The string with the delimiter escaped as in URLs
     * @see #ATTRIB_DELIM
     */
    public static String escape(String inStr, char delimchar) {
        // integer equivalent of the delimiter
        int delim = delimchar;

        // convert this to a '%<int>' string
        String delimEscStr = "%" + delim;

        // the buffer to return
        final StringBuilder outBuffer = new StringBuilder(inStr);

        int index = 0;
        int delimIndex = inStr.indexOf(delimchar, index);
        while (delimIndex != -1) {
            // delete the delimiter and add the escape string
            outBuffer.deleteCharAt(delimIndex);
            outBuffer.insert(delimIndex, delimEscStr);

            index = delimIndex + delimEscStr.length() + 1;
            delimIndex = outBuffer.toString().indexOf(delimchar, index);
        }

        return outBuffer.toString();
    }

    @Autowired
    private org.springframework.transaction.support.TransactionOperations transactionOperations;

    private final LoadingCache<String, AlertTemplate> eventTemplateCache;

    private final ExpandableParameterResolverRegistry resolverRegistry = new ExpandableParameterResolverRegistry();

    public AbstractAlertUtil() {
        // Build the cache, and enable statistics collection if we've been given a metric registry
        final long maximumCacheSize =
                Long.parseLong(System.getProperty("org.opennms.eventd.eventTemplateCacheSize", "1000"));
        final CacheBuilder<Object, Object> cacheBuilder =
                CacheBuilder.newBuilder().maximumSize(maximumCacheSize);
        eventTemplateCache = cacheBuilder.build(new CacheLoader<String, AlertTemplate>() {
            public AlertTemplate load(String key) throws Exception {
                return new AlertTemplate(key, AbstractAlertUtil.this);
            }
        });
    }

    /**
     * Helper method.
     *
     * @param event
     * @return All event parameter values as a String.
     */
    public static String getAllParmValues(org.opennms.horizon.events.proto.Event event) {
        String retParmVal = null;
        if (!event.getParametersList().isEmpty()) {
            final StringBuilder ret = new StringBuilder();

            for (EventParameter evParm : event.getParametersList()) {
                String parmValue = evParm.getValue();
                if (parmValue.isEmpty()) continue;

                if (ret.isEmpty()) {
                    ret.append(parmValue);
                } else {
                    ret.append(SPACE_DELIM).append(parmValue);
                }
            }

            retParmVal = ret.toString();
        }
        return retParmVal;
    }

    /**
     * Helper method.
     * @return The names of all the event parameters.
     */
    public static String getAllParmNames(org.opennms.horizon.events.proto.Event event) {
        if (event.getParametersList().isEmpty()) {
            return null;
        } else {
            final StringBuilder ret = new StringBuilder();

            for (EventParameter evParm : event.getParametersList()) {
                String parmName = evParm.getName();
                if (parmName.isEmpty()) continue;

                if (ret.isEmpty()) {
                    ret.append(parmName.trim());
                } else {
                    ret.append(SPACE_DELIM).append(parmName.trim());
                }
            }
            return ret.toString();
        }
    }

    /**
     * Helper method.
     *
     * @param event
     * @return All event parameter values as a String
     */
    public static String getAllParamValues(final org.opennms.horizon.events.proto.Event event) {
        if (event.getParametersList().isEmpty()) {
            return null;
        } else {
            final StringBuilder ret = new StringBuilder();

            for (final EventParameter evParm : event.getParametersList()) {
                final String parmName = evParm.getName();
                if (parmName.isEmpty()) continue;

                final String parmValue = evParm.getValue();
                if (parmValue.isEmpty()) continue;

                if (!ret.isEmpty()) {
                    ret.append(SPACE_DELIM);
                }
                ret.append(parmName.trim())
                        .append(NAME_VAL_DELIM)
                        .append("\"")
                        .append(parmValue)
                        .append("\"");
            }

            return ret.toString().intern();
        }
    }

    /**
     * Helper method.
     *
     * @param parm
     * @param event
     * @return The name of a parameter based on its ordinal position in the event's list of parameters
     */
    public static String getNumParmName(String parm, Event event) {
        String retParmVal = null;
        final List<EventParameter> parms = event.getParametersList();
        int end = parm.lastIndexOf(PARM_END_SUFFIX);
        if (end != -1 && !parms.isEmpty()) {
            // Get the string between the '#' and ']'
            String parmSpec = parm.substring(PARM_NAME_NUMBERED_PREFIX_LENGTH, end);
            String eparmnum = null;
            String eparmsep = null;
            String eparmoffset = null;
            String eparmrangesep = null;
            String eparmrangelen = null;
            if (parmSpec.matches("^\\d+$")) {
                eparmnum = parmSpec;
            } else {
                Matcher m = Pattern.compile("^(\\d+)([^0-9+-]+)([+-]?\\d+)((:)([+-]?\\d+)?)?$")
                        .matcher(parmSpec);
                if (m.matches()) {
                    eparmnum = m.group(1);
                    eparmsep = m.group(2);
                    eparmoffset = m.group(3);
                    eparmrangesep = m.group(5);
                    eparmrangelen = m.group(6);
                }
            }
            int parmNum = -1;
            try {
                assert eparmnum != null;
                parmNum = Integer.parseInt(eparmnum);
            } catch (NumberFormatException nfe) {
                parmNum = -1;
            }

            if (parmNum > 0 && parmNum <= parms.size()) {
                final EventParameter evParm = parms.get(parmNum - 1);

                // get parm name
                String eparmname = evParm.getName();

                // If separator and offset specified, split and extract accordingly
                if ((eparmsep != null) && (eparmoffset != null)) {
                    int parmOffset = Integer.parseInt(eparmoffset);
                    boolean doRange = ":".equals(eparmrangesep);
                    int parmRangeLen = (eparmrangelen == null) ? 0 : Integer.parseInt(eparmrangelen);
                    retParmVal = splitAndExtract(eparmname, eparmsep, parmOffset, doRange, parmRangeLen);
                } else {
                    retParmVal = eparmname;
                }
            } else {
                retParmVal = null;
            }
        }
        return retParmVal;
    }

    public static String splitAndExtract(String src, String sep, int offset, boolean doRange, int rangeLen) {
        String sepLiteral = Pattern.quote(sep);

        // If the src string starts with the separator, lose the first separator
        if (src.startsWith(sep)) {
            src = src.replaceFirst(sepLiteral, "");
        }

        String[] components = src.split(sepLiteral);
        int startIndex;
        int endIndex;
        if ((Math.abs(offset) > components.length) || (offset == 0)) {
            return null;
        } else if (offset < 0) {
            startIndex = components.length + offset;
        } else {
            // offset is, by definition, > 0
            startIndex = offset - 1;
        }

        endIndex = startIndex;

        if (!doRange) {
            return components[startIndex];
        } else if (rangeLen == 0) {
            endIndex = components.length - 1;
        } else if (rangeLen < 0) {
            endIndex = startIndex + 1 + rangeLen;
        } else {
            // rangeLen is, by definition, > 0
            endIndex = startIndex - 1 + rangeLen;
        }

        final StringBuilder retVal = new StringBuilder();
        for (int i = startIndex; i <= endIndex; i++) {
            retVal.append(components[i]);
            if (i < endIndex) {
                retVal.append(sep);
            }
        }
        return retVal.toString();
    }

    /**
     * Helper method.
     *
     * @param parm
     * @param event
     * @return The value of a parameter based on its ordinal position in the event's list of parameters
     */
    public static String getNumParmValue(String parm, org.opennms.horizon.events.proto.Event event) {
        String retParmVal = null;
        final List<EventParameter> parms = event.getParametersList();
        int end = parm.lastIndexOf(PARM_END_SUFFIX);
        if (end != -1 && !parms.isEmpty()) {
            // Get the value between the '#' and ']'
            String eparmname = parm.substring(PARM_NUM_PREFIX_LENGTH, end);
            int parmNum = -1;
            try {
                parmNum = Integer.parseInt(eparmname);
            } catch (NumberFormatException nfe) {
            }

            if (parmNum > 0 && parmNum <= parms.size()) {
                final EventParameter evParm = parms.get(parmNum - 1);

                // get parm value
                retParmVal = evParm.getValue();
            } else {
                retParmVal = null;
            }
        }
        return retParmVal;
    }

    /**
     * Helper method.
     *
     * @param parm a {@link java.lang.String} object.
     *
     * @return A parameter's value as a String using the parameter's name..
     */
    public String getNamedParmValue(String parm, Event event) {
        final Matcher matcher = PARM_REGEX.matcher(parm);
        if (!matcher.matches()) {
            return null;
        }

        final String eparmname = matcher.group(1);
        final EventParameter evParm = getParmTrim(eparmname, event);
        if (evParm != null) {
            return evParm.getValue();
        }
        return null;
    }

    /**
     * <p>expandMapValues</p>
     *
     * @param map a {@link java.util.Map} object.
     *
     */
    public void expandMapValues(final Map<String, String> map, final Event event) {
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            final String key = entry.getKey();
            final String mapValue = entry.getValue();
            if (mapValue == null) {
                continue;
            }
            final String expandedValue = expandParms(map.get(key), event);
            if (expandedValue == null) {
                // Don't use this value to replace the existing value if it's null
            } else {
                map.put(key, expandedValue);
            }
        }
    }

    /**
     * Expand the value if it has parms in one of the following formats -
     * %element% values are expanded to have the value of the element where
     * 'element' is an element in the event DTD - %parm[values-all]% is expanded
     * to a delimited list of all parmblock values - %parm[names-all]% is
     * expanded to a list of all parm names - %parm[all]% is expanded to a full
     * dump of all parmblocks - %parm[name]% is expanded to the value of the
     * parameter named 'name' - %parm[ <name>]% is replaced by the value of the
     * parameter named 'name', if present - %parm[# <num>]% is replaced by the
     * value of the parameter number 'num', if present - %parm[##]% is replaced
     * by the number of parameters
     *
     * @param inp
     *            the input string in which parm values are to be expanded
     * @return expanded value if the value had any parameter to expand, null
     *         otherwise
     *
     */
    public String expandParms(String inp, Event event) {
        return expandParms(inp, event, null);
    }

    /**
     * Expand the value if it has parms in one of the following formats -
     * %element% values are expanded to have the value of the element where
     * 'element' is an element in the event DTD - %parm[values-all]% is expanded
     * to a delimited list of all parmblock values - %parm[names-all]% is
     * expanded to a list of all parm names - %parm[all]% is expanded to a full
     * dump of all parmblocks - %parm[name]% is expanded to the value of the
     * parameter named 'name' - %parm[ <name>]% is replaced by the value of the
     * parameter named 'name', if present - %parm[# <num>]% is replaced by the
     * value of the parameter number 'num', if present - %parm[##]% is replaced
     * by the number of parameters
     *
     * @param input
     *            the input string in which parm values are to be expanded
     * @param decode
     *            the varbind decode for this
     * @return expanded value if the value had any parameter to expand, null
     *         otherwise
     *
     */
    public String expandParms(String input, Event event, Map<String, Map<String, String>> decode) {
        if (input == null) {
            return null;
        }
        try {
            final AlertTemplate eventTemplate = eventTemplateCache.get(input);
            Supplier<String> expander = () -> eventTemplate.expand(event, decode);
            if (eventTemplate.requiresTransaction()) {
                Objects.requireNonNull(transactionOperations);
                return transactionOperations.execute(session -> expander.get());
            } else {
                return expander.get();
            }
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * <p>getEventHost</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getEventHost(final Event event) {
        if (event.getHost().isEmpty()) {
            return null;
        }

        // If the event doesn't have a node ID, we can't lookup the IP address and be sure we have the right one since
        // we don't know what node it is on
        if (event.getNodeId() <= 0) {
            return event.getHost();
        }

        try {
            return getHostName(event.getNodeId(), event.getHost(), event.getTenantId());
        } catch (final Exception t) {
            LOG.warn("Error converting host IP \"{}\" to a hostname, storing the IP.", event.getHost(), t);
            return event.getHost();
        }
    }

    @Override
    public ExpandableParameterResolver getResolver(String token) {
        return resolverRegistry.getResolver(token);
    }

    public static EventParameter getParmTrim(String key, Event event) {
        List<EventParameter> parms = event.getParametersList();
        for (final EventParameter parm : parms) {
            if (StringUtils.equalsTrimmed(parm.getName(), key)) {
                return parm;
            }
        }

        return null;
    }
}
