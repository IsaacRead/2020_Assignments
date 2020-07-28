public class test{

public static void main(String args[]){

MyList<String> strings = new MyList<String>("a", new MyList<String>("a", null));
System.out.println(count(strings, "a")); // returns 1

MyList<Integer> nums = new MyList<Integer>(4, new MyList<Integer>(4, null));
System.out.println(count(nums, 42)); // returns 2

} 

public static <T> int count( MyList<T> list, T item){
int count = 0;
while(true){
if (list.head() == item) count++;
if (list.tail() != null) list = list.tail();
else return count;

}

}

}
class MyList<T> {
 private T item;
 private MyList<T> next;
 public MyList(T i, MyList<T> t) { item = i; next = t; }
 public T head() { return item; }
 public MyList<T> tail() { return next; }



}
