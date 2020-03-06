package com.github.jferrater.opa.ast.to.sql.query.core.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.jferrater.opa.ast.to.sql.query.model.response.Value;
import com.github.jferrater.opa.ast.to.sql.query.model.response.Predicate;
import com.github.jferrater.opa.ast.to.sql.query.model.response.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.jferrater.opa.ast.to.sql.query.core.OpaConstants.*;

public class PredicateDeserializer extends JsonDeserializer<Predicate> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PredicateDeserializer.class);

    @Override
    public Predicate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectCodec objectCodec = jsonParser.getCodec();
        JsonNode jsonNode = objectCodec.readTree(jsonParser);
        JsonNode termsNode = jsonNode.get(TERMS);
        List<Term> terms = new ArrayList<>();
        for (JsonNode termNode : termsNode) {
            Term term = new Term();
            String type = termNode.get(TYPE).asText();
            term.setType(type);
            JsonNode valueNodes = termNode.get(VALUE);
            if (valueNodes.isArray()) {
                List<Value> valueList = addValuesToList(valueNodes);
                term.setValue(valueList);
            } else {
                setValueForTerm(valueNodes, term);
            }
            terms.add(term);
        }
        return buildPredicate(jsonNode, terms);
    }

    private List<Value> addValuesToList(JsonNode valueNodes) {
        List<Value> valueList = new ArrayList<>();
        for (JsonNode valueNode : valueNodes) {
            Value value = getValue(valueNode);
            valueList.add(value);
        }
        return valueList;
    }

    private Value getValue(JsonNode valueNode) {
        Value value = new Value();
        String valueType = valueNode.get(TYPE).asText();
        value.setType(valueType);
        JsonNode nodeValue = valueNode.get(VALUE);
        switch (valueType) {
            case NUMBER:
                value.setValues(nodeValue.asInt());
                break;
            case STRING:
            case VAR:
                value.setValues(nodeValue.asText());
                break;
            default:
                LOGGER.warn("Value type '{}' is not yet supported", valueNode.toPrettyString());
        }
        return value;
    }

    private void setValueForTerm(JsonNode jsonNode, Term term) {
        if (jsonNode.isInt()) {
            term.setValue(jsonNode.asInt());
        } else if (jsonNode.isTextual()) {
            term.setValue(jsonNode.asText());
        } else {
            LOGGER.warn("Query type '{}' is not yet supported", jsonNode.toPrettyString());
        }
    }

    private Predicate buildPredicate(JsonNode jsonNode, List<Term> terms) {
        Predicate predicate = new Predicate();
        JsonNode indexNode = jsonNode.get(INDEX);
        predicate.setIndex(indexNode.asInt());
        predicate.setTerms(terms);
        return predicate;
    }
}