package fr.commons.generique.model.db;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
public abstract class AbstractObjetBddAvecId implements IObjetBdd, Serializable {
	
	private static final long serialVersionUID = -970725977018002771L;

	public static final long NO_ID = -1L;

	protected long id = NO_ID;


	public AbstractObjetBddAvecId() {
	}

	public AbstractObjetBddAvecId(long id) {
		this.id = id;
	}

	@Override
	public boolean isNew() {
		return this.id == NO_ID;
	}
}