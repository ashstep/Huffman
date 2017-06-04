import java.util.PriorityQueue;

public class HuffProcessor implements Processor{
	String[] array = new String[ALPH_SIZE+1];
	public void compress(BitInputStream in, BitOutputStream out){
		//as long as u havent reached the end of the file
		int[] anArray = new int[ALPH_SIZE];
		int num = in.readBits(BITS_PER_WORD);
		while(num != -1){
			anArray[num]++;	
			num = in.readBits(BITS_PER_WORD);}
		in.reset();
		//loop through char and create huffman for each
		PriorityQueue<HuffNode> pq = new PriorityQueue<HuffNode>(ALPH_SIZE+1);
		for(int i = 0 ; i < 256 ; i++){
			if (anArray[i] != 0){
				HuffNode a = new HuffNode(i, anArray[i]);
				pq.add(a);}}
		//PSEUDO EOF is not included when printing alphabet
		HuffNode ps = new HuffNode(PSEUDO_EOF,0);
		pq.add(ps);
		while(pq.size() > 1 ){
			//poll two smallest nodes -> combine them into one huffnode
			HuffNode left = pq.poll();
			HuffNode right = pq.poll();
			HuffNode combined = new HuffNode(-1, left.weight() + right.weight(), left, right);
			pq.add(combined);}
		HuffNode current = pq.poll();
		extractCodes(current,"");
		//write out huffnumber
		out.writeBits(BITS_PER_INT, HUFF_NUMBER);
		writeHeader(current,  out);
		//compress/write the body
		int num2 = in.readBits(BITS_PER_WORD);
		while(num2 != -1){
			String code = array[num2];	
			out.writeBits(code.length(), Integer.parseInt(code, 2));
			num2 = in.readBits(BITS_PER_WORD);}
		String variable = array[PSEUDO_EOF];
		out.writeBits(variable.length(), Integer.parseInt(variable, 2));}

	private void extractCodes(HuffNode current, String path){
		//when u reach leafnode
		if ((current.left() == null) && (current.right() == null)){
			//add to data structure
			array[current.value()] = path;
			return;
		}else{
			extractCodes(current.left(), path + 0);
			extractCodes(current.right(), path + 1);}}

	private void writeHeader(HuffNode current, BitOutputStream out){
		if ((current.left() == null) && (current.right() == null)){
			out.writeBits(1, 1);
			out.writeBits(9, current.value());
		}else{
			out.writeBits(1, 0);
			writeHeader(current.left(), out);
			writeHeader(current.right(), out);}}
	
	
	public void decompress(BitInputStream in, BitOutputStream out){
		//check for huffnumber
		if(in.readBits(BITS_PER_INT) != HUFF_NUMBER){
			throw new HuffException("Huff Exception");}
		//parse body of the compressed file
		HuffNode root = readHeader(in);
		int num = in.readBits(1);
		HuffNode current = root;
		while(num != -1){
			if(num == 1){current = current.right();}
			else {current = current.left();}
			//if its a leaf -> is it the right value?
			if((current.left() == null) && (current.right() == null)){
				if(current.value() == PSEUDO_EOF){
					return;}
				else{out.writeBits(8, current.value());
					current = root;}}
			num = in.readBits(1);}
		throw new HuffException("Exception");}
	//recreate tree from header
	private HuffNode readHeader(BitInputStream in){
		int num = in.readBits(1);
		if(num == 0){
			HuffNode left = readHeader(in);
			HuffNode right = readHeader(in);
			return new HuffNode(-1, left.weight() + right.weight(), left, right);
		}else{
			int num9 = in.readBits(9);
			return new HuffNode(num9, 0);}}}


