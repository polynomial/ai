package com.cyster.conversation;

public class Message {

	public enum Type {
		SYSTEM("System"),
		AI("Ai"),
		USER("User"),
		ERROR("Error"),
		INFO("Info");
		
		private final String name;
		
		Type(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
	}
	
	private Type type;
	private String content;
	
	public Message(Type type, String content) {
		this.type = type;
		this.content = content;
	}

	public Message(String content) {
		this.type = Type.USER;
		this.content = content;
	}
	
	public Type getType() {
		return this.type;
	}
	
	public String content() {
		return this.content;
	}
}
