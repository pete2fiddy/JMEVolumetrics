package mygame.util;

import java.util.HashMap;
import java.util.Map;

public abstract class ArgumentContainer {
    private Object[] args;
    private Map<String, Integer> namedArgInds = new HashMap<String, Integer>();
    
    public ArgumentContainer(Object... args) {
        this.args = args;
        String[] argNames = argNames();
        for(int i = 0; i < argNames.length; i++) {
            namedArgInds.put(argNames[i], i);
        }
        assert(argNames.length == args.length);
    }
    
    public Object get(int ind) {return args[ind];}
    public Object get(String name) {return args[namedArgInds.get(name)];}
    protected abstract String[] argNames();
}
