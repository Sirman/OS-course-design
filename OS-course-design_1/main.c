#include <stdio.h>
#include <stdlib.h>
#include <time.h>

typedef struct PCB  //进程控制块PCB
{
    int id;         //进程标识数
    int status;     //进程状态  0-可执行   1-等待状态1: 表示输出井满，请求输出的用户等待    2-等待状态2 :表示请求输出井空SPOOLING输出进程等待
    //3-等待状态3 :表示请求输出井满 请求输出的进程等待   4-结束态 :进程执行完成
    int count;      //要输入的文件数
    int x;          //进程输出时的临时变量
} ;

typedef struct Reqblock
{
    int reqname;        //请求进程名
    int length;         //本次输出的长度
    int addr;           //信息在输出井的首地址
} ;

int menu()
{
    int i;
    printf("请选择操作");
    printf("1-init 2-down 3-over\n");
    scanf("%d",&i);
    return i;
}

void Srand()
{
    int i;
    srand((unsigned) time(NULL));
    for(i=0; i<10; i++)
        printf("%d\t",rand()%10);
}

//完成每个进程的PCB块的初始化、输出请求块、输出井初始化
void Init(struct PCB pcb[], struct Reqblock req[])
{
    int out1=0,out2=0;
    int out_1=0,out_2=0;
    int buffer[2][100];
    int c1[2]= {100,100};
    int c2[2][2]= {0,0,0,0};
    int c3=10;
    int pt1=0,pt2=0;
    int i;
    for(i=1; i<=3; i++)
        {
            pcb[i].count=0;
            pcb[i].id=0;
            pcb[i].x=0;
            pcb[i].status=0;
        }
    for(i=0; i<10; i++)
        {
            req[i].addr=0;
            req[i].length=0;
            req[i].reqname=0;
        }
}

//进程调度
void run(struct PCB pcb[], struct Reqblock req[])
{
    do
        {
            int random =rand()%10;
            if(random<=0.45 && pcb[1].status==0)
                {
                    printf("调度进程1");
                }
            else if(random>0.45 && random<=0.9 && pcb[2].status==0)
                {
                    printf("调度进程2");
                }
            else if(random>=0.9 && random<1 && pcb[3].status==0)
                {
                    printf("调度SPOOLING进程");
                }
        }
    while(pcb[1].status!=4 || pcb[2].status!=4 || pcb[3].status!=4);
}

int user(int name,int out,int pt1,int pt2,int file_count,int random,int c3,int c1[], int c2[][2],int buffer[][100],struct PCB  pcb[],struct Reqblock req[])
{
    int x;
    pcb[name].id=name;
    pcb[name].count=file_count;
    while(out!=pcb[name].count)
        {
            c2[name][1]=c2[name][0]  ;
            do
                {
                    x=random;
                    pcb[name].x=x;
                    if(c1[name]==0)
                        {
                            pcb[name].status=1;
                            if(c2[name][0]>=c2[name][1])
                                {
                                    c1[name]=c1[name]+c2[name][0]-c2[name][1];
                                }
                            else
                                c1[name]=c1[name]+100-c2[name][1]+c2[name][0];
                            c2[name][0]=c2[name][1];
                        }
                    else                                                                                                //若输出井满
                        {
                            buffer[name][c2[name][0]]=pcb[name].x;				//进程的输出信息PCB[i].x送如buffer[i][c2[i][0]]
                            c1[name]=c1[name]-1;																//输出井空闲个数减1
                            c2[name][0]=(c2[name][0]+1)%100;								//修改空缓冲区指针C2[i][0]前进1
                        }
                }
            while(x!=0);
            printf("第%d个文件已放如输出井 : %d ~ %d\t剩余空间   %d",out+1,c2[name][1],c2[name][0],c1[name]);
            out++;
            if(c3==0)																								//若没有空闲请求输出块，转为等待状态3
                {
                    pcb[name].status=3;
                    if(c2[name][0]>=c2[name][1])
                        c1[name]=c1[name]+c2[name][0]-c2[name][1];
                    else
                        c1[name]=c1[name]+100-c2[name][1]+c2[name][0];
                    c2[name][0]=c2[name][1];
                    out--;
                    printf("缺少请求输出块");
                    printf("进入等待状态3");
                    out++;                                                                                                              //放在这有什么意义？？？？？？？？？？？？？？？？？
                    return out;
                }
            else																																					//若有空闲请求输出块
                {
                    req[pt2].addr=c2[name][1];																		//将文件在输出井的位置填入空闲请求块
                    if(c2[name][0]>=c2[name][1])																				//将文件在输出井的长度填入空闲请求块
                        {
                            req[pt2].length=c2[name][0]-c2[name][1];
                        }
                    else
                        req[pt2].length=100-c2[name][1]+c2[name][0];
                    req[pt2].reqname=name;																				//将进程名i填入请求块
                    printf("获得请求数据块%d\n",pt2+1);
                    pt2=(pt2+1)%10;																																						//修改空闲请求块指针
                    c3--;																																													//空闲请求块数减1
                    if(pcb[3].status==2)																																			//若SPOOLING	进程是等待状态、则唤醒SPOOLING进程
                        {
                            pcb[3].status=0;
                        }
                }
        }
    printf("进程%d输入完毕",name+1);
    pcb[name].status=4;
    return out;
}

void spooling1(int c3,int pt1,int pt2,int out_1,int out_2,int c1[],int c2[][2],int buffer[][100],struct PCB pcb[],struct Reqblock req[])
{
    int i;
    while(c3!=0)
        {
            char buffer1 [100];
            for(i=0; i<req[pt1].length; i++)
                {
                    //				buffer1.append(buffer[reqblock[pt1].repname][reqblock[pt1].addr]);req[pt1].addr=(req[pt1].addr+1)%100
                    printf("%d",buffer[req[pt1].reqname][req[pt1].addr]);
                    req[pt1].addr=(req[pt1].addr+1)%100;
                }
            if(req[pt1].reqname==1)
                {
                    out_1++;
                    printf("输出进程1第%d个文件的内容",out_1);
                }
            else
                {
                    out_2++;
                    printf("输出进程2第%d个文件的内容",out_2);
                }
            //			spooling.textarea3.append(buffer1.toString()+"\n");
            c1[req[pt1].reqname]=c1[req[pt1].reqname]+req[pt1].length;
            pt1=(pt1+1)%10;
            c3++;
            int k;
            for(k=1; i<=2; i++)
                {
                    if(pcb[k].status==1)																				//有等待的输出井的进程、唤醒相应进程，转进程调度
                        {
                            pcb[k].status=0;
                            return;
                        }
                }
            for(k=1; k<=2; k++)
                {
                    //有等待请求输出块的进程，唤醒相应进程
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
            printf("Spooling输出进程结束");
            return ;
        }
    else									//输出进程等待
        {
            pcb[3].status=2;
            return;
        }
}

int main()
{
    int file1_count,file2_count;
    struct PCB pcb[4];
    struct Reqblock req[10];
    printf("用户进程1的文件数:\t");
    scanf("%d",&file1_count);
    printf("用户进程2的文件数");
    scanf("%d",&file2_count);
    Init(pcb,req);
    run(pcb,req);
    return 0;
}
