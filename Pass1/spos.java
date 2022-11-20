import java.io.*;
class spos 
{
public static void main(String args[]) throws NullPointerException, FileNotFoundException
{
String REG[] = {"ax","bx","cx","dx"};
String IS[]={"stop","add","sub","mult","mover","movem","comp","bc","div","read"};
String DL[]={"ds","dc"};
obj[] literal_table = new obj[10];
obj[] symb_table = new obj[10];
obj[] optab =new obj[60];
pooltable[] pooltab=new pooltable[5];
String line; 
try{
BufferedReader br=new BufferedReader(new FileReader("sampal.txt"));
BufferedWriter bw=new BufferedWriter(new FileWriter("output.txt"));
Boolean start=false;
Boolean end=false,fill_addr=false,ltorg=false;
int total_symb=0,total_ltr=0,optab_cnt=0,pooltab_cnt=0,loc=0,temp,pos;
while((line=br.readLine())!=null&&!end)
 {
 String tokens[]=line.split(" ",4);
 if(loc!=0 && !ltorg)
 {
 bw.write("\n"+String.valueOf(loc));
 ltorg=false;
 loc++;
 }
 ltorg=fill_addr=false;
 for(int k=0;k<tokens.length;k++)
 {
 pos = -1;
 if(start==true)
 {
 loc=Integer.parseInt(tokens[k]);
 start=false;
 }
 
 switch(tokens[k])
{
 case "start" : start = true;
 pos = 1;
 bw.write("\t(AD,"+pos+")");
 break;
 case "end": end=true;
 pos = 2;
 bw.write("\t(AD,"+pos+")\n");
 for(temp=0;temp<total_ltr;temp++)
 if(literal_table[temp].addr==0)
 { literal_table[temp].addr=loc-1;
 bw.write("\t(DL,1)\t(C,"+literal_table[temp].name+")"+"\n"+loc++);
 }
 /* if(pooltab_cnt==0)
 pooltab[pooltab_cnt++]=new pooltable(0,temp);
 else
 {
 pooltab[pooltab_cnt]=new pooltable(pooltab[pooltab_cnt-1].first+pooltab[pooltab_cnt-1].total_literals,total_ltr-pooltab[pooltab_cnt-1].first-1);
 pooltab_cnt++;
 } */
 break;
 
 case "origin": pos = 3; 
 bw.write("\t(AD,"+pos+")");
 pos= search(tokens[++k],symb_table,total_symb);
 k++;
 bw.write("\t(C,"+(symb_table[pos].addr)+")");
 loc = symb_table[pos].addr;
 break;
 
 case "ltorg": ltorg=true;
 pos = 5;
 for(temp=0;temp<total_ltr;temp++)
 if(literal_table[temp].addr==0)
 {
 literal_table[temp].addr=loc-1;
 bw.write("\t(DL,1)\t(C,"+literal_table[temp].name+")"+"\n"+loc++);
 }
 if(pooltab_cnt==0)
 pooltab[pooltab_cnt++]=new pooltable(0,temp);
 else
 {
 pooltab[pooltab_cnt]=new pooltable(pooltab[pooltab_cnt-1].first+pooltab[pooltab_cnt-1].total_literals,total_ltr-pooltab[pooltab_cnt-1].first-1);
 pooltab_cnt++;
 } 
 break;
 
 case "equ": pos = 4; 
 bw.write("\t(AD,"+pos+")");
 String prev_token=tokens[k-1];
 int pos1 = search(prev_token,symb_table,total_symb);
 pos = search(tokens[++k],symb_table,total_symb);
 symb_table[pos1].addr = symb_table[pos].addr;
 bw.write("\t(S,"+(pos+1)+")");
 break;
 
}
 if(pos == -1)
 {
 pos=search(tokens[k], IS);
 if(pos != -1)
 {
 bw.write("\t(IS,"+(pos+1)+")");
 optab[optab_cnt++]=new obj(tokens[k], pos);
 }
 else
 {
 pos=search(tokens[k], DL);
 if(pos != -1)
 {
 bw.write("\t(DL,"+(pos+1)+")");
 optab[optab_cnt++]=new obj(tokens[k], pos);
 fill_addr=true;
 }
 else if(tokens[k].matches("[a-zA-Z]+:"))
 {
 pos = search(tokens[k], symb_table,total_symb);
 if(pos == -1)
 {
 symb_table[total_symb++]=new obj(tokens[k].substring(0,tokens[k].length()-1),loc-1);
 bw.write("\t(S,"+total_symb+")");
 pos=total_symb;
 }
 }
 }
 }
 if(pos == -1) 
 {
 pos=search(tokens[k], REG);
 if(pos!=-1)
 bw.write("\t(RG,"+(pos+1)+")");
 else
 {
 if(tokens[k].matches("='\\d+'"))
 {
 String s=tokens[k].substring(2, 3);
 
 literal_table[total_ltr++]=new obj(s, 0);
 bw.write("\t(L,"+total_ltr+")");
 }
 else if(tokens[k].matches("\\d+")||tokens[k].matches("\\d+H") || tokens[k].matches("\\d+h") || 
tokens[k].matches("\\d+D") || tokens[k].matches("\\d+d"))
 bw.write("\t(C,"+tokens[k]+")");
 else
 {
 pos = search(tokens[k], symb_table,total_symb);
 if(fill_addr && pos!=-1)
{
symb_table[pos].addr=loc-1;
fill_addr=false;
}
 else if(pos==-1)
 {
 symb_table[total_symb++]=new obj(tokens[k],0);
 bw.write("\t(S," + total_symb + ")");
 }
 else
 bw.write("\t(S," + pos + ")");
 }
 }
 }
 }
 }
 System.out.println("\n******************SYMBOL TABLE*********************");
 System.out.println("\nSYMBOL\tADDRESS");
 for(int i=0;i<total_symb;i++)
 System.out.println(symb_table[i].name+"\t"+symb_table[i].addr);
 
 pooltab[pooltab_cnt]=new pooltable(pooltab[pooltab_cnt-1].first+pooltab[pooltab_cnt-1].total_literals,total_ltr-pooltab[pooltab_cnt-1].first-2);
pooltab_cnt++;
 System.out.println("\n****************POOL TABLE*******************");
 System.out.println("\nPOOL\tTOTAL LITERALS");
 
 for(int i=0;i<pooltab_cnt;i++)
 System.out.println(pooltab[i].first+"\t"+pooltab[i].total_literals);
 
 System.out.println("\n**************LITERAL TABLE***************");
 System.out.println("\nIndex\tLITERAL\tADDRESS");
 for(int i=0;i<total_ltr;i++)
 {
 if(literal_table[i].addr==0)
 literal_table[i].addr=loc++;
 System.out.println((i) +"\t"+literal_table[i].name+"\t"+literal_table[i].addr);
 }
 
 System.out.println("\n***************OPTABLE*********************");
 System.out.println("\nMNEMONIC\tOPCODE");
 for(int i=0;i<IS.length;i++)
 System.out.println(IS[i]+"\t\t"+i);
 
 br.close();
 bw.close();
}
catch(Exception e)
{
 System.out.println("error while reading the file");
 e.printStackTrace();
}
BufferedReader br=new BufferedReader(new FileReader("output.txt"));
System.out.println("\n*********Output1.txt****************\n");
try {
while((line=br.readLine())!=null)
System.out.println(line);
br.close();
} catch (IOException e) {
// TODO Auto-generated catch block
e.printStackTrace();
}
} 
public static int search(String token, String[] list) {
for(int i=0;i<list.length;i++)
if(token.equalsIgnoreCase(list[i]))
return i;
return -1;
}
public static int search(String token, obj[] list,int cnt) {
for(int i=0;i<cnt;i++)
if(token.equalsIgnoreCase(list[i].name))
return i;
return -1;
}
} 