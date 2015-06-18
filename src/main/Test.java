package main;
import com.google.common.collect.TreeMultiset;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.Set;
import utilities.G;

public class Test {

    public static void main(String[] args) throws IOException, ParseException {

	System.out.println("hey");
	
	
	Set<Integer> set1 = new LinkedHashSet();
	Set<Integer> set2 = new LinkedHashSet();

	set1.add(0);
	set1.add(1);
	set1.add(2);
	set1.add(3);
	set1.add(4);
	set1.add(5);
	set1.add(6);
	set1.add(7);
//
//	set2.add(5);
//	set2.add(6);
//	set2.add(7);

	set1.retainAll(set2);

	G.asdf(set1);
	System.exit(0);


	double dd = 0;

	int x = 4;

	dd = 4 * 0.02;

	System.out.println(dd);

	TreeMultiset<MyClass> ts = TreeMultiset.create();

	ts.add(new MyClass(1, 2));
	ts.add(new MyClass(1, 5));
	ts.add(new MyClass(2, 2));
	ts.add(new MyClass(2, 3));
	ts.add(new MyClass(1, 3));
	ts.add(new MyClass(2, 4));
	ts.add(new MyClass(2, 5));
	ts.add(new MyClass(3, 2));
	ts.add(new MyClass(3, 3));
	ts.add(new MyClass(3, 4));
	ts.add(new MyClass(1, 4));
	ts.add(new MyClass(3, 5));


	G.asdf(ts.size());

	assert ts.size() == 130;

	G.asdf(ts);
	for (MyClass mc : ts) {
	    G.asdf(mc);
	}
	G.asdf();


    }

    private static void doStuff() throws DummyException {
	throw new DummyException("here is my input message");
    }

    /*
     TODO -- seeing if we can send all class attributes to a map with their variable name and value, without having to retype the name anywwhere (for eradatarow reference map for screening )
     */

    public static class MyClass implements Comparable<MyClass> {
	public char a;
	private byte b;
	public int c = 2;
	public float d;

	int x;

	@Override
	public int compareTo(MyClass o) {
	    return Integer.compare(x, o.x);
	}

	@Override
	public String toString() {
	    return x + " " + c;
	}

	public MyClass(int x) {
	    this.x = x;
	    a = 1;
	    b = 2;
	    c = 3;
	    d = 9;
	}

	private MyClass() {
	}

	private MyClass(int x, int c) {
	    this.x = x;
	    this.c = c;
	}

	private boolean hereIam() throws IllegalArgumentException, IllegalAccessException {

	    Class c = MyClass.class;

	    Field[] fs = c.getFields();

	    MyClass mc = new MyClass();

	    String name = fs[0].getName();

	    for (Field f : fs) {
		System.out.println(f.getName());
//		System.out.println(f.getDouble(this));
		System.out.println(f.getType());
	    }


	    return true;
	}

    }

    private static class DummyException extends Exception {

	public DummyException(String here_is_my_input_message) {
	    super(here_is_my_input_message);
	}
    }


}
