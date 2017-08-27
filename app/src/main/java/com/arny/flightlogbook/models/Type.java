package com.arny.flightlogbook.models;

import org.chalup.microorm.annotations.Column;
public class Type {
	@Column("type_id")
	private int typeId;
	@Column("airplane_type")
	private String typeName;

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
}
