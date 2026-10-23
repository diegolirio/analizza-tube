package br.com.mytube.domain.rules;

import br.com.mytube.domain.BusinessRule;

/** Campo obrigatório de entrada: quebra quando ausente (null) ou, para texto, em branco. */
public class RequiredFieldRule extends BusinessRule {
    private final Object value;

    public RequiredFieldRule(String field, Object value) {
        super("O campo '" + field + "' é obrigatório.", "required." + field);
        this.value = value;
    }

    @Override
    public boolean isBroken() {
        return value == null || (value instanceof CharSequence text && text.toString().isBlank());
    }
}
