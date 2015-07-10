import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.lang.Math;
import java.util.Random;
public class Spooling extends JFrame implements ActionListener, Runnable
{
	//static JFrame jfrm = new JFrame();
	JPanel panel1,panel2,panel3;
	JTextField field1,field2; 
	JScrollPane p1,p2,p3,p4; 
	JTextArea textarea1,textarea2,textarea3,textarea4; 
	JButton button1,button2,button3; 
	Manage spoo;
	
	//初始化界面
	public Spooling()
	 {
		 spoo=new Manage(this);
		 Container c=this.getContentPane();
		 c.setLayout(new BorderLayout());
		 //jfrm.setLayout(new BorderLayout());
		 
		 field1=new JTextField(3);  //设置panel1 
		 field2=new JTextField(3); 
		 button1=new JButton("运行"); 
		 button2=new JButton("关闭 "); 
		 button3=new JButton("重置 ");
		 button1.addActionListener(this); 
		 button2.addActionListener(this); 
		 button3.addActionListener(this);
		 
		 panel1 = new JPanel();
		 panel1.setLayout(new FlowLayout());
		 panel1.add(new  JLabel("用户进程1文件数:", SwingConstants.RIGHT));
		 panel1.add(field1);
		 panel1.add(new  JLabel("用户进程2文件数:", SwingConstants.RIGHT));
		 panel1.add(field2);
		 panel1.add(button1);
		 panel1.add(button2);
		 panel1.add(button3); 			//设置panel1完毕
		 
		 textarea1=new JTextArea(80,100);  // 设置panel2 
		 textarea2=new JTextArea(112,400); 
		 textarea3=new JTextArea(112,400); 
		 textarea1.append(" 用户进程 1的输出\n"); 
		 textarea2.append(" 用户进程 2 的输出 \n"); 
		 textarea3.append("Spooling的调度 \n");
		 p1=new JScrollPane(textarea3);
		 p2=new JScrollPane(textarea1);
		 p3=new JScrollPane(textarea2);
		 
		 panel2 = new JPanel();
		 panel2.setLayout(new GridLayout(1,3));
		 panel2.add(p1);
		 panel2.add(p2);
		 panel2.add(p3);					//设置paenl2完毕
		 
		 textarea4 = new JTextArea(10,150);
		 textarea4.append("主程序调度过程\n");
		 p4 = new JScrollPane(textarea4);
		 panel3=new JPanel();
		 panel3.setLayout(new GridLayout(1,1));
		 panel3.add(p4);					//设置panel3完毕
		 
		 c.add(panel1, BorderLayout.NORTH);			//设置窗口
		 c.add(panel2, BorderLayout.CENTER);
		 c.add(panel3, BorderLayout.SOUTH);
		 
		 this.setSize(1200, 600);
		 this.setLocation(100,100);
		 this.setTitle("Spooling");
		 this.setVisible(true);
		 this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 
	 }
	
	//事件监听
	public void actionPerformed(ActionEvent e)
	 {
		if(e.getSource()==button1)
		{
			run();
		}
		if(e.getSource()==button2)
		{
			System.exit(0);
		}
		if(e.getSource()==button3)
		{
			begin();
		}
	}
	
	//重置数据
	public void begin()
	{
		field1.setText("");
		field2.setText("");
		textarea1.setText("用户进程1的输出");
		textarea2.setText("用户进程2的输出");
		textarea3.setText("Spooling的调度");
		textarea4.setText("主程序调度过程\n");
	}
	
	//运行
	public void run()
	{
		spoo.start();
	}
	
	//主函数
	public static void main(String[] args) 
	{
		Spooling spooling = new Spooling();
	}
}
class Manage extends Thread
{
	PCB pcb[];			
	Reqblock reqblock[];
	int buffer[][];
	int c1[];				//可使用的输出井buffer空间
	int c2[][];			//输出井buffer空闲和满指针
	int c3;						//reqblock的剩余个数
	int pt1;					//要输出的第一个空闲的reqblock指针
	int pt2;					//第一个空闲reqblock指针
	double random;		//用于调度三个进程的控制的控制随机数
	int out1;				//用户进程1已生成的文件数
	int out2;				//用户进程2已生成的文件数
	int out_1;				//用户进程1已输出的文件数
	int out_2;				//用户进程2已输出的文件数
	int x;						//随机生成的数据0～9
	int i;						//临时控制变量
	Random x1;				//辅助生成随机数据x:0~9;
	Spooling spooling;
	
	public Manage(Spooling spooling1)
	{
		out1=0;out2=0;
		out_1=0;out_2=0;
		pcb = new PCB[4];									//初始化进程块
		reqblock = new Reqblock[10];		//初始化请求块
		buffer = new int [2][100];			//初始化输出井
		c1 = new int [2];									//初始化输出井计数器						
		c1[0]=100;c1[1]=100;
		c2 = new int [2][2];							//初始化输出井的 空闲和满指针
		c2[0][0]=0;c2[1][0]=0;						//00和10分别表示buffer[1]和buffer[2]的空闲指针
		c3=10;																//初始化请求块计数器
		pt1=0;
		pt2=0;
		x1=new Random();
		for(i=0;i<4;i++)
		{
			pcb[i]=new PCB();
		}
		for(i=0;i<10;i++)
		{
			reqblock[i]=new Reqblock();
		}
		for(i=1;i<=3;i++)
		{
			pcb[i].status=0;
		}
		//spooling1 = new Spooling();
		spooling = spooling1;
	}
	public void run()				//进程调度
	{
		do
		{
			random=Math.random();
			if(random<=0.45 && pcb[1].status==0)
			{
				spooling.textarea4.append("调度用户进程1\n");
				try
				{
					sleep(1000);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				out1=user(0,out1,spooling.textarea1,spooling.field1);
			}
			else if(random>0.45 && random<=0.9 && pcb[2].status==0)
			{
				spooling.textarea4.append("调度用户进程2\n");
				try
				{
					sleep(1000);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				out2=user(1,out2,spooling.textarea2,spooling.field2);
			}
			else if(random>=0.9&&random<1&&pcb[3].status==0)
			{
				spooling.textarea4.append("调度SPOOLING进程\n");
				try
				{
					sleep(1000);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				spooling1();
			}
		}while(pcb[1].status!=4 || pcb[2].status!=4 || pcb[3].status!=4);
		spooling.textarea4.append("程序运行结束\n");								//进程调度结束
	}
	public int user(int name,int out,JTextArea textarea,JTextField field)			//用户进程
	{
		pcb[name].id=name;
		pcb[name].count = Integer.parseInt(field.getText());
		while(out!=pcb[name].count)				//判断进程所要输出的文件是否完毕的while循环
		{
			c2[name][1]=c2[name][0];
			do																				//判断进程的一个文件是否输出完毕的while循环
			{
				x=x1.nextInt(9);										//x为每次随机生成的数据0～9,送入pcb.x
				pcb[name].x=x;											
				if(c1[name]==0)										//若输出井buffer满，变为等待状态1,转调度程序
				{
					pcb[name].status=1;
					if(c2[name][0]>=c2[name][1])
					{
						c1[name]=c1[name]+c2[name][0]-c2[name][1];
					}
					else
						c1[name]=c1[name]+100-c2[name][1]+c2[name][0];
					c2[name][0]=c2[name][1];
					textarea.append("第" + (out+1)+"个文件缺少输出井");
					textarea.append("进入等待状态1\n");
					try
					{
						sleep(1000);
					}
					catch(InterruptedException e)
					{
						e.printStackTrace();
					}
					//return out;
				}
				else																												//若输出井满
				{
					buffer[name][c2[name][0]]=pcb[name].x;				//进程的输出信息PCB[i].x送如buffer[i][c2[i][0]]
					c1[name]=c1[name]-1;																//输出井空闲个数减1
					c2[name][0]=(c2[name][0]+1)%100;								//修改空缓冲区指针C2[i][0]前进1;
				}
			}while( x!=0);																						//判断进程的一个文件是否输出完毕的while循环结束
			textarea.append("第"+(out+1)+"个文	件已放入输出	井："+c2[name][1]+"	～	"+(c2[name][0]-1)+"    	剩余空间"+c1[name]+"。");
			try
			{
				sleep(1000);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			out ++;
			if(c3==0)																								//若没有空闲请求输出块，转为等待状态3
			{
				pcb[name].status=3; 
				if(c2[name][0]>=c2[name][1]) 
					c1[name]=c1[name]+c2[name][0]-c2[name][1]; 
				else 
					c1[name]=c1[name]+100-c2[name][1]+c2[name][0]; 
				c2[name][0]=c2[name][1]; 
				out--;
				textarea.append("缺少请求输出块");
				textarea.append("进入等待状态3\n");
				try
				{
					sleep(1000);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				out++;
				return out;
			}
			else																																					//若有空闲请求输出块
			{
				reqblock[pt2].addr=c2[name][1];																		//将文件在输出井的位置填入空闲请求块
				if(c2[name][0]>=c2[name][1])																				//将文件在输出井的长度填入空闲请求块
				{
					reqblock[pt2].length=c2[name][0]-c2[name][1];
				}
				else
					reqblock[pt2].length=100-c2[name][1]+c2[name][0];
				reqblock[pt2].repname=name;																				//将进程名i填入请求块
				textarea.append("获得请求数据块"+(Integer.toString(pt2+1))+"\n");
				pt2=(pt2+1)%10;																																						//修改空闲请求块指针
				c3--;																																													//空闲请求块数减1
				if(pcb[3].status==2)																																			//若SPOOLING	进程是等待状态、则唤醒SPOOLING进程
				{
					pcb[3].status=0;
				}
			}
		}				//判断进程所要输出的文件是否输出完毕的while循环结束 
		textarea.append("进程" + (name+1) + "输入完毕\n");				//文件输出完毕,修改状态为结束，转进程调度
		pcb[name].status=4;
		return out;
	}
	public void spooling1()
	{
		while(c3!=10)						//判断请求输出块是否为空的while循环
		{														//若请求输出快不为空
			StringBuffer buffer1 = new StringBuffer(100);
			for(i=0;i<reqblock[pt1].length;i++)				//按该请求输出信息块reqlock[]指针ptr1将输出井中的一个文件的内容放入临时buffer1中
			{
				buffer1.append(buffer[reqblock[pt1].repname][reqblock[pt1].addr]);
				reqblock[pt1].addr=(reqblock[pt1].addr+1)%100;
			}
			if(reqblock[pt1].repname==1)
			{
				out_1++;
				spooling.textarea3.append("输出进程1第"+out_1+"个文件的内容:");
			}
			else
			{
				out_2++;
				spooling.textarea3.append("输出进程2第"+out_2+"个文件的内容:");
			}
			spooling.textarea3.append(buffer1.toString()+"\n");
			try
			{
				sleep(1000);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			//释放相应输出井、即修改相应的输出井计数从c1
			c1[reqblock[pt1].repname]=c1[reqblock[pt1].repname]+reqblock[pt1].length;
			pt1=(pt1+1)%10;
			c3++;
			int k;
			for(k=1;i<=2;i++)
			{
				if(pcb[k].status==1)																				//有等待的输出井的进程、唤醒相应进程，转进程调度
				{
					pcb[k].status=0;
					return;
				}
			}
			for(k=1;k<=2;k++)
			{																																		//有等待请求输出块的进程，唤醒相应进程
				if(pcb[k].status==3)
				{
					pcb[k].status=0;
					return;
				}
			}
		}	//判断请求块是否为空的while循环结束
		if(pcb[1].status==4 && pcb[2].status==4)				//进程1、2结束后输入进程结束
		{
			pcb[3].status=4;
			spooling.textarea3.append("Spooling输出进程结束");
			return ;
		}
		else									//输出进程等待
		{
			pcb[3].status=2;
			return;
		}
	}
}
class PCB
{
	int id;					//进程标识数
	int status;		//进程状态
	int count;			//要输出的文件数
	int x;					//进程输出时的临时变量
}
class Reqblock
{
	int repname;		//请求进程名
	int length;			//本次输出信息长度
	int addr;				//信息在输出井的首地址
}
