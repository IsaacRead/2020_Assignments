public class BSTlex{
	//isaac read 1347707
	public String key;	
	public BSTlex left, right;
	
	//insert value k into BST
	public boolean insert(String k){		
		//if current key is empty insert value here 
		if(key == null){
			key = k;
			//create empty children nodes for future 
			left = new BSTlex();
			right = new BSTlex();
			System.out.println(key + " INSERTED");	
			return true;
		}
		else{
			//test if current key is greater or less than key to be inserted (alphabetically)
			int value = k.compareTo(key);
			//if equal return false indicating insert key is already in BST
			if(value == 0){				
				return false;
			}
			System.out.print(key + " ");
			//if less than insert into left branch
			if(value < 0){
				return left.insert(k); 			
			}
			//if greater than insert into right branch
			else{
				return right.insert(k);
			}
		}
	}
	
	//print all values in BST
	public void transverse(){
		//if current is empty dont do anything
		if (key == null) return;
		//if current has left branch transverse left branch
		if(left != null) left.transverse();
		//print current key
		System.out.println(key);
		//if current has right branch transverse right branch
		if(right != null) right.transverse();		
		}
	//find specified value in BST
	public BSTlex find(String k){
		//return null if current key is null
		if(this.key == null){		
		return null;	
		}
		
		//test if current key is greater or less than key to be inserted (alphabetically)
		int value = k.compareTo(key);
		//if equal then specified key has been found
		if(value == 0){			 
			return this;
		}		
		
		//if less then search in left brancj
		if(value < 0){
			BSTlex f = left.find(k);
			if (f != null) return f;

		}
		//else search in right branch
		else{
			BSTlex f = right.find(k);
			if (f != null) return f;
		}	
		return null;
		
	}
		//removes current from BST
		public void remove(){	
			//if key is already null do nothing
			if(this.key != null){				
				//if no children 
				if(this.left.key == null && this.right.key == null){
					//set current key to null
					this.key = null;					
					return;
				}				
				//2 children
				else if (this.left.key != null && this.right.key != null){	
						//find leftmost in right branch 
						BSTlex succ = findSucc();
						//copy key from successor to this
						this.key = succ.key;
						//remove successor
						succ.remove();
				}
				//one child
				else if(this.left.key != null){	
					//set this to values of left child branch
					this.key = this.left.key;
					this.right = this.left.right;
					this.left = this.left.left;
					
				}
				else if(this.right.key != null){
					//set this to values of right child branch
					this.key = this.right.key;					
					this.left = this.right.left;
					this.right = this.right.right;
					
				}				
			}
		}
		
	//finds leftmost in right branch
		public BSTlex findSucc(){
			BSTlex succ = this.right;
			while (succ.left != null && succ.left.key != null){
				succ = succ.left;
			}
			return succ;		
		}
	}
	
	
	
		
	
