package graphql.execution;

import graphql.PublicApi;
import graphql.language.Argument;
import graphql.language.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static graphql.Assert.assertNotEmpty;

/**
 * This represent all Fields in a query which overlap and are merged into one.
 * This means they all represent the same field actually when the query is executed.
 *
 * Example query with more than one Field merged together:
 *
 * <pre>
 * {@code
 *
 *      query Foo {
 *          bar
 *          ...BarFragment
 *      }
 *
 *      fragment BarFragment on Query {
 *          bar
 *      }
 * }
 * </pre>
 *
 * Another example:
 * <pre>
 * {@code
 *     {
 *          me{fistName}
 *          me{lastName}
 *     }
 * }
 * </pre>
 *
 * Here the me field is merged together including the sub selections.
 *
 * A third example with different directives:
 * <pre>
 * {@code
 *     {
 *          foo @someDirective
 *          foo @anotherDirective
 *     }
 * }
 * </pre>
 * These examples make clear that you need to consider all merged fields together to have the full picture.
 *
 * The actual logic when fields can successfully merged together is implemented in {#graphql.validation.rules.OverlappingFieldsCanBeMerged}
 */
@PublicApi
public class MergedField {

    private final List<Field> fields;

    private MergedField(List<Field> fields) {
        assertNotEmpty(fields);
        this.fields = new ArrayList<>(fields);
    }

    /**
     * All merged fields have the same name.
     *
     * WARNING: This is not always the key in the execution result, because of possible aliases.
     *
     * @return the name of of the merged fields.
     */
    public String getName() {
        return fields.get(0).getName();
    }

    /**
     * The first of the merged fields.
     *
     * Because all fields are almost identically
     * often only one of the merged fields are used.
     *
     * @return the fist of the merged Fields
     */
    public Field getSingleField() {
        return fields.get(0);
    }

    /**
     * All merged fields share the same arguments.
     *
     * @return
     */
    public List<Argument> getArguments() {
        return getSingleField().getArguments();
    }


    /**
     * All merged fields
     *
     * @return all merged fields
     */
    public List<Field> getFields() {
        return new ArrayList<>(fields);
    }

    public static Builder newMergedField() {
        return new Builder();
    }

    public static Builder newMergedField(Field field) {
        return new Builder().addField(field);
    }

    public static Builder newMergedField(List<Field> fields) {
        return new Builder().fields(fields);
    }

    public MergedField transform(Consumer<Builder> builderConsumer) {
        Builder builder = new Builder(this);
        builderConsumer.accept(builder);
        return builder.build();
    }

    public static class Builder {
        private List<Field> fields = new ArrayList<>();

        private Builder() {

        }

        private Builder(MergedField existing) {
            this.fields = existing.getFields();
        }

        public Builder fields(List<Field> fields) {
            this.fields = fields;
            return this;
        }

        public Builder addField(Field field) {
            this.fields.add(field);
            return this;
        }

        public MergedField build() {
            return new MergedField(fields);
        }


    }

}
