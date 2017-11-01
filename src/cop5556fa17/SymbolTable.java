package cop5556fa17;

import java.util.ArrayList;
import java.util.HashMap;

import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Statement;

public class SymbolTable {
	
	/*static class attrs {
		Declaration dec;
	}*/
	Declaration dec;
	
	HashMap<String, Declaration> symbol_map;
	//ArrayList<attrs> values;
	
	public boolean insert(String name, Declaration dec) {
		if (lookupType(name) != null) {
			return false;
		}
		symbol_map.put(name, dec);
		return true;
	}
	
	public Declaration lookupType(String name) {
		if (symbol_map.containsKey(name)) {
			return symbol_map.get(name);
		}
		return null;
	}
	
	public SymbolTable() {
		symbol_map = new HashMap<>();
	}
	
	@Override
	public String toString() {
		String sumbol_map_string = symbol_map.toString();
		return "HashMap: " + sumbol_map_string;
	}
}
