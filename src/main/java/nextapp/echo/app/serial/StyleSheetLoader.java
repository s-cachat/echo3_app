/* 
 * This file is part of the Echo Web Application Framework (hereinafter "Echo").
 * Copyright (C) 2002-2009 NextApp, Inc.
 * Copyright (C) 2010-2025 Stéphane Cachat
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */
package nextapp.echo.app.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import nextapp.echo.app.DerivedMutableStyle;
import nextapp.echo.app.Extent;
import nextapp.echo.app.MutableStyleSheet;
import nextapp.echo.app.Style;
import nextapp.echo.app.StyleSheet;
import nextapp.echo.app.util.DomUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Loads style sheet data from XML format into a <code>StyleSheet</code>
 * instance.
 */
public class StyleSheetLoader {

    /**
     * Parses an XML style sheet and returns a <code>StyleSheet</code> instance.
     * <p>
     * Styles for components that cannot be loaded by the specified
     * <code>ClassLoader</code> will be ignored.
     *
     * @param resourceName the name of the resource on the
     * <code>CLASSPATH</code> containing the XML data
     * @param classLoader the <code>ClassLoader</code> with which to instantiate
     * property objects
     * @return the created <code>StyleSheet</code> or null if the resource does
     * not exist
     * @throws SerialException if parsing/instantiation errors occur
     */
    public static StyleSheet load(String resourceName, ClassLoader classLoader)
            throws SerialException {
        InputStream in = null;
        try {
            in = classLoader.getResourceAsStream(resourceName);
            if (in == null) {
                return null;
            }
            return load(in, classLoader);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    /**
     * Parses an XML style sheet and returns a <code>StyleSheet</code> instance.
     * <p>
     * Styles for components that cannot be loaded by the specified
     * <code>ClassLoader</code> will be ignored.
     *
     * @param in the <code>InputStream</code> containing the XML data
     * @param classLoader the <code>ClassLoader</code> with which to instantiate
     * property objects
     * @return the created <code>StyleSheet</code>
     * @throws SerialException if parsing/instantiation errors occur
     */
    public static StyleSheet load(InputStream in, final ClassLoader classLoader)
            throws SerialException {
        final Document document;
        try {
            DocumentBuilder builder = DomUtil.getDocumentBuilder();
            document = builder.parse(in);
        } catch (IOException ex) {
            throw new SerialException("Failed to parse InputStream.", ex);
        } catch (SAXException ex) {
            throw new SerialException("Failed to parse InputStream.", ex);
        }

        Map namedStyleMap = new HashMap();

        MutableStyleSheet styleSheet = new MutableStyleSheet();
        Element styleSheetElement = document.getDocumentElement();

        Element[] constantElements = DomUtil.getChildElementsByTagName(styleSheetElement, "c");
        Serializer serializer = Serializer.forClassLoader(classLoader);
        Constants styleConstants = new Constants();
        // First pass, load constants
        for (int i = 0; i < constantElements.length; ++i) {
            final Element element = constantElements[i];
            styleConstants.put(element);
        }
        styleConstants.resolve();
        for (Map.Entry<String, Constant> c : styleConstants.getConstants().entrySet()) {
            final Constant cst = c.getValue();
            if ("Extent".equals(cst.getType()) && cst.getNumericValue() != null) {
                int unit = switch (cst.getUnit()) {
                    case "cm" ->
                        Extent.CM;
                    case "em" ->
                        Extent.EM;
                    case "ex" ->
                        Extent.EX;
                    case "in" ->
                        Extent.IN;
                    case "mm" ->
                        Extent.MM;
                    case "PC" ->
                        Extent.PC;
                    case "percent" ->
                        Extent.PERCENT;
                    case "pt" ->
                        Extent.PT;
                    case "px" ->
                        Extent.PX;
                    default ->
                        -1;
                };
                if (unit > 0) {
                    styleSheet.add(c.getKey(), new Extent(cst.getNumericValue().intValue(), unit));
                }
            }
        }
        Element[] styleElements = DomUtil.getChildElementsByTagName(styleSheetElement, "s");
        // Second pass, replace constants
        for (int i = 0; i < styleElements.length; ++i) {
            final Element element = styleElements[i];
            replaceConstant(element, styleConstants);
        }
        try {
            DomUtil.save(document, new PrintWriter("/tmp/out.stylesheet"), new Properties());
        } catch (IOException | SAXException ex) {
            ex.printStackTrace();
        }
        // Third pass, load style information.
        for (int i = 0; i < styleElements.length; ++i) {
            final Element element = styleElements[i];
            String name = element.getAttribute("n");
            if ("".equals(name)) {
                name = null;
            }
            if (!element.hasAttribute("t")) {
                throw new SerialException("Component type not specified in style: " + name, null);
            }
            String type = element.getAttribute("t");

            Class componentClass;
            try {
                componentClass = serializer.getClass(type);
            } catch (ClassNotFoundException ex) {
                // StyleSheet contains reference to Component which does not exist in this ClassLoader,
                // and thus should be ignored.
                continue;
            }

            DerivedMutableStyle style = new DerivedMutableStyle();

            SerialContext context = new SerialContext() {

                public ClassLoader getClassLoader() {
                    return classLoader;
                }

                public int getFlags() {
                    return 0;
                }

                public Document getDocument() {
                    return document;
                }
            };

            Style propertyStyle = serializer.loadStyle(context, type, element);
            style.addStyleContent(propertyStyle);

            Map classToStyleMap = (Map) namedStyleMap.get(name);
            if (classToStyleMap == null) {
                classToStyleMap = new HashMap();
                namedStyleMap.put(name, classToStyleMap);
            }
            classToStyleMap.put(componentClass, style);

            styleSheet.addStyle(componentClass, name, style);
        }

        // Fourth pass, bind derived styles to base styles where applicable.
        for (int i = 0; i < styleElements.length; ++i) {
            if (styleElements[i].hasAttribute("b")) {
                String name = styleElements[i].getAttribute("n");
                String type = styleElements[i].getAttribute("t");
                Class componentClass;
                try {
                    componentClass = Class.forName(type, true, classLoader);
                } catch (ClassNotFoundException ex) {
                    // StyleSheet contains reference to Component which does not exist in this ClassLoader,
                    // and thus should be ignored.
                    continue;
                }

                Map classToStyleMap = (Map) namedStyleMap.get(name);
                DerivedMutableStyle style = (DerivedMutableStyle) classToStyleMap.get(componentClass);

                String baseName = styleElements[i].getAttribute("b");

                classToStyleMap = (Map) namedStyleMap.get(baseName);
                if (classToStyleMap == null) {
                    throw new SerialException("Invalid base style name for style name " + name + "(type " + type + ").", null);
                }
                Style baseStyle = (Style) classToStyleMap.get(componentClass);
                while (baseStyle == null && componentClass != Object.class) {
                    componentClass = componentClass.getSuperclass();
                    baseStyle = (Style) classToStyleMap.get(componentClass);
                }
                if (baseStyle == null) {
                    throw new SerialException("Invalid base style name for style name " + name + ".", null);
                }

                style.setParentStyle(baseStyle);
            }
        }

        return styleSheet;
    }

    /**
     * remplace les références aux constantes par les constantes
     *
     * @param element l'élément à traiter
     * @param styleConstants les constantes
     * @throws SerialException en cas d'erreur
     */
    private static void replaceConstant(Element element, Constants styleConstants) throws SerialException {
        final String nodeName = element.getNodeName() + "[" + element.getAttribute("n") + "]";
        String nodeType = element.getAttribute("t");
        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Node a = element.getAttributes().item(i);
            if (a instanceof Attr attr) {
                String v = attr.getValue();
                String nv = calc(nodeType, v, styleConstants);
                if (nv != null) {
                    attr.setValue(nv);
                }
            } else {
                throw new SerialException("not an attribute : " + a, null);
            }
        }
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node a = element.getChildNodes().item(i);
            if (a instanceof Element child) {
                replaceConstant(child, styleConstants);
            } else if (a instanceof Text) {
            } else if (a instanceof Comment) {
            } else {
                throw new SerialException("not an attribute : " + a + " : " + a.getClass().getName(), null);
            }
        }
        String v = DomUtil.getElementText(element);
        String nv = calc(nodeType, v, styleConstants);
        if (nv != null) {
            DomUtil.setElementText(element, nv);
        }
    }

    /**
     * calcule la valeur de la constante
     *
     * @param type le type (utilisé par exemple pour les insets, ce qui indique
     * que la valeur est composée d'une suite de 2 ou 4 valeurs séparées par des
     * espaces)
     * @param v la valeur (commençant par @)
     * @param styleConstants les constantes
     * @return la nouvelle valeur si elle a changée, ou null si elle n'aurait
     * pas changée
     * @throws SerialException
     */
    private static String calc(String type, String v, Constants styleConstants) throws SerialException {
        if (v == null) {
            return null;
        } else if (isMultiValued(type)) {
            String[] vl = v.split(" ");
            StringBuilder sb = new StringBuilder();
            for (String vi : vl) {
                if (vi.startsWith("@")) {
                    sb.append(sb.length() == 0 ? "" : " ").append(calc(vi.trim(), styleConstants));
                } else {
                    sb.append(sb.length() == 0 ? "" : " ").append(vi);
                }
            }
            String vr = sb.toString();
            return vr.equals(v) ? null : vr;
        } else {
            if (v.startsWith("@")) {
                String vr = calc(v, styleConstants);
                return vr.equals(v) ? null : vr;
            } else {
                return null;
            }
        }
    }

    /**
     * calcule la valeur de la constante
     *
     * @param v la valeur (commençant par @)
     * @param styleConstants les constantes
     * @return la nouvelle valeur
     * @throws SerialException
     */
    private static String calc(String v, Constants styleConstants) throws SerialException {
        String nv = v.substring(1);
        Constant nc = styleConstants.getConstants().get(nv);
        if (nc != null && nc.getNumericValue() == null) {//la constante n'est pas numérique, pas la peine d'aller plus loin
            return nc.getValue();
        }
        try {
            String unit = null;
            Matcher mat = BasicMath.variablePattern.matcher(nv);
            while (mat.find()) {
                Constant c = styleConstants.getConstants().get(mat.group(0));
                if (c != null) {
                    String u = c.getUnit();
                    if (u != null && !u.isBlank()) {
                        unit = u;
                        break;
                    }
                }
            }
            nv = formatNumeric(BasicMath.evaluate(nv, styleConstants.getNumericConstants()), unit);
        } catch (Exception ex) {
            throw new SerialException("can't evaluate : " + nv, null);
        }
        return nv;
    }
    /**
     * pattern pour reconnaitre les valeurs numériques
     */
    private static final Pattern CONSTANT_SPLIT = Pattern.compile("(\\d+(\\.\\d*)?)(.*)?");

    /**
     * formate une valeur numérique
     *
     * @param number la valeur numérique
     * @param unit l'unité optionelle (peut être null)
     * @return la valeur formatée
     */
    private static String formatNumeric(double number, String unit) {
        String num = String.format(Locale.ENGLISH, "%f", number);
        if (num.contains(".")) {
            int l = num.length() - 1;
            while (l > 0 && num.charAt(l) == '0') {
                l--;
            }
            if (num.charAt(l) == '.') {
                l--;
            }
            num = num.substring(0, l + 1);
        }
        if (unit == null) {
            return num;
        } else {
            return num + unit;
        }
    }

    /**
     * une constante
     */
    private static class Constant {

        /**
         * valeur texte complete
         */
        private String value;
        /**
         * valeur numérique ou null
         */
        private Double numericValue;
        /**
         * unité
         */
        private String unit;
        /**
         * type
         */
        private String type;
        /**
         * l'élément du dom
         */
        private final Element element;

        /**
         * Constructeur
         *
         * @param element l'élément du dom
         * @param value la valeur brute
         */
        public Constant(Element element, String type, String value) {
            this.value = value;
            this.type = type;
            this.element = element;
            String fi;
            if ((!isMultiValued(type) || !value.contains(" ")) || value.length() <= 1) {
                Matcher mat = CONSTANT_SPLIT.matcher(value);
                if (mat.matches()) {
                    numericValue = Double.valueOf(mat.group(1));
                    unit = mat.group(3);
                }
            }
        }

        /**
         * @return valeur texte complete
         */
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
            DomUtil.setElementText(element, value);
        }

        /**
         * return valeur numérique ou null
         */
        public Double getNumericValue() {
            return numericValue;
        }

        /**
         * @return unité
         */
        public String getUnit() {
            return unit;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

    /**
     * le conteneur des constantes
     */
    private static class Constants {

        /**
         * table des constantes complete
         */
        private final Map<String, Constant> constants = new HashMap<>();
        /**
         * table des constantes numériques
         */
        private final Map<String, Double> numericConstants = new HashMap<>();

        /**
         * @return table des constantes complete
         */
        public Map<String, Constant> getConstants() {
            return constants;
        }

        /**
         * @return table des constantes numériques
         */
        public Map<String, Double> getNumericConstants() {
            return numericConstants;
        }

        /**
         * ajoute une constante
         *
         * @param name le nom
         * @param value la valeur
         * @param type le type (peut être null)
         */
        public void put(Element element) throws SerialException {
            String name = element.getAttribute("n");
            String type = element.getAttribute("t");
            String value = element.hasAttribute("v") ? element.getAttribute("v") : DomUtil.getElementText(element);
            if (name == null || name.isBlank()) {
                throw new SerialException("constant with no name " + name + " = " + value, null);
            }
            if (value == null || value.isBlank()) {
                throw new SerialException("constant with no value " + name + " = " + value, null);
            }
            Constant c = new Constant(element, type, value);
            constants.put(name, c);
            if (c.getNumericValue() != null) {
                numericConstants.put(name, c.getNumericValue());
            }
        }

        /**
         * résoud les constantes calculées à partir d'autres constantes
         */
        public void resolve() throws SerialException {
            Map<String, Double> numericConstantsWithUnresolved = new HashMap<>(numericConstants);
            boolean modified = true;
            while (modified) {//premières passes : constantes texte et valeurs simple, remplissage de la table des non résolues
                modified = false;
                for (Map.Entry<String, Constant> x : constants.entrySet()) {
                    Constant c = x.getValue();
                    String v = c.getValue();
                    if (v.startsWith("@") && !(isMultiValued(c.type) && v.contains(" "))) {
                        Constant r = constants.get(v.substring(1));
                        if (r != null) {
                            c.numericValue = r.numericValue;
                            c.unit = r.unit;
                            c.setValue(r.getValue());
                            if (c.numericValue != null) {
                                numericConstants.put(x.getKey(), c.numericValue);
                                numericConstantsWithUnresolved.put(x.getKey(), c.numericValue);
                            } else {
                                numericConstantsWithUnresolved.remove(x.getKey());
                            }
                            modified = true;
                        } else {
                            numericConstantsWithUnresolved.put(x.getKey(), Double.NaN);
                        }
                    }
                }
            }

            modified = true;
            while (modified) {//passes suivantes : résolution globale des valeurs numériques
                modified = false;
                for (Map.Entry<String, Constant> x : constants.entrySet()) {
                    Constant c = x.getValue();
                    if (c.getNumericValue() == null) {
                        String v = c.getValue();
                        if (isMultiValued(c.type) && v.contains(" ")) {//valeur non numérique (plusieurs nombres)                            
                        } else {
                            if (v.startsWith("@")) {
                                v = v.substring(1);
                                try {
                                    double n = BasicMath.evaluate(v, numericConstantsWithUnresolved);
                                    if (Double.isNaN(n)) {
                                        continue;
                                    }
                                    String unit = findUnit(v);
                                    c.numericValue = n;
                                    c.unit = unit;
                                    c.setValue(formatNumeric(c.numericValue, c.unit));
                                    numericConstants.put(x.getKey(), n);
                                    numericConstantsWithUnresolved.put(x.getKey(), n);
                                    modified = true;
                                } catch (Exception ex) {
                                    throw new SerialException("Error in formula : " + v, ex);
                                }
                            }
                        }
                    }
                }
            }
            for (Map.Entry<String, Constant> x
                    : constants.entrySet()) {
                Constant c = x.getValue();
                if (c.getNumericValue() == null) {
                    String v = c.getValue();
                    if (isMultiValued(c.type) && v.contains(" ")) {//valeur non numérique (plusieurs nombres)
                        String[] vl = v.split(" ");
                        StringBuilder sb = new StringBuilder();
                        for (String vi : vl) {
                            if (vi.startsWith("@")) {
                                try {
                                    double nv = BasicMath.evaluate(vi.substring(1), numericConstants);
                                    String unit = findUnit(vi);
                                    sb.append(sb.length() == 0 ? "" : " ").append(nv);
                                    if (unit != null) {
                                        sb.append(unit);
                                    }
                                } catch (Exception ex) {
                                    throw new SerialException("Error in formula : " + vi + " (origin : " + v + ")", ex);
                                }
                            } else {
                                sb.append(sb.length() == 0 ? "" : " ").append(vi);
                            }
                        }
                        String vr = sb.toString();
                        c.setValue(vr);
                    }
                }
            }
        }

        private String findUnit(String nv) {
            String unit = "";
            Matcher mat = BasicMath.variablePattern.matcher(nv);
            while (mat.find()) {
                Constant cst = constants.get(mat.group(0));
                if (cst != null) {
                    unit = cst.getUnit();
                    break;
                }
            }
            return unit;
        }
    }

    /**
     * la propriété est elle constituée de plusieurs valeurs séparées par des
     * espaces. Exemple insets, border
     *
     * @param type le type de la propriété
     * @return true si multi valeur
     */
    private static boolean isMultiValued(String type) {
        return "Insets".equals(type) || "Border".equals(type);
    }

}
