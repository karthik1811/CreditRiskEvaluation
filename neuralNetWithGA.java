import java.sql.*;
import java.io.*;
import java.util.*;
import java.lang.*;

public class neuralNetWithGA {

	private static Connection connect = null;
	  private static Statement statement = null;
	  private static PreparedStatement preparedStatement = null;
	  private static ResultSet resultSet = null;  
	  private static ResultSet wholeTable;
	  private static String table="german";//the table in which normalisation is to be done
	  private static int totrows;
	  private static double trainingratio=0.3;
	  private static double learningRate = 0.0081;
	  private static double minError = 0.008;
	  private static int hiddenno;
	  private static int inputno;
	  private static double hiddenOut[];
	  private static double finalOut;	
	  private static double MomentumFactor = 0.7;
	  private static ResultSet trainingRows;
	  private static int truePositive;
	  private static int trueNegative;
	  private static int TotalTruePositive;
	  private static int TotalTrueNegative;
	  private static ResultSet allrows;
	  private static int trainrowsno;
	  private static double firstconn[][];
	  private static double secondfirstconn[][];
	  private static double secondconn[];
	  private static double secondsecondconn[];
	  public static int population[][];
	  public static double fitness[];
	  public static int finalpopulation[];
	  public static int GaTrainIterations=10;
	  public static int NeuralTrainIterations=1;
	  public static int initPopNo=6;	  
	  public static double mutateRate=0.001;
	  
	  public static void intializations()
	  {
		  try
		  {
			  	 Class.forName("com.mysql.jdbc.Driver");
				 // setup the connection with the DB.
				 connect = DriverManager.getConnection("jdbc:mysql://localhost/semproject?" + "user=ggk&password=ggk");
				 
				 statement = connect.createStatement();
				 wholeTable = statement.executeQuery("select * from german");
				 inputno=wholeTable.getMetaData().getColumnCount();
				 inputno=inputno-1;
				 System.out.println("The input number is:"+inputno);			 
				
				 statement = connect.createStatement();
				 ResultSet temp;
				 temp = statement.executeQuery("select count(*) as count from "+table);
				  
				  while(temp.next())
				  {
					  totrows = temp.getInt("count");
				  }
				  trainrowsno =  (int) (totrows * trainingratio);
				  fitness = new double[initPopNo];
				  /*System.out.println("Please enter the number of hidden layer nodes:");
				  Scanner in = new Scanner(System.in);
				  hiddenno = in.nextInt();*/
                  hiddenno=18;
				  System.out.println(hiddenno);
				  /*wholeTable.next();
				  for(int i=0;i<900;i++)
				  {
					  //System.out.println("1");
					  //System.out.println(wholeTable.getFloat(1));
					  System.out.println(wholeTable.getFloat("att1"));
					  //System.out.println("2");
					  //System.out.println(wholeTable.getFloat(2));
					  System.out.println(wholeTable.getFloat("att2"));
					  //System.out.println("3");
					  //System.out.println(wholeTable.getFloat(3));
					  System.out.println(wholeTable.getFloat("att3"));
					  //System.out.println();
					  wholeTable.next();
				  }*/
					 
		  }
		  catch(Exception e)
		  {
			  e.printStackTrace();
		  }
				 
	  }
	  
	  public static void createIntialPopulation()//put the initial population in population[] 
	  {
		 /* Doing it using 2D integer array may lead to same parents...*/
		  
		 population = new int[6][inputno]; //Initial population size is taken as 6
		 System.out.println("The population lenght is:"+population.length);
		 Random randomGenerator = new Random();
		 for(int i=0;i<6;i++)
		 {
			 for(int j=0;j<inputno;j++)
			 {
				 population[i][j] = randomGenerator.nextInt(2);
				 System.out.print(population[i][j]);
			 }
			 System.out.println();
		 }
		 
	  }
	  
	  
	  public static void createNeuralNetwork()
	  {
			  try
			  {
			  	//create the entire neural network. don't worry about the featureSelect here.
			  	//we'll take care of that at the points here we do the operations
			  	 
				 firstconn = new double[inputno][hiddenno];
				 secondfirstconn = new double[inputno][hiddenno];
				 double sign,magnitude;
				 
				 for(int i=0;i<inputno;i++)
				 {
					 for(int j=0;j<hiddenno;j++)
					 {
						 if(Math.random()<0.5)
							 sign=0;
						 else
							 sign=1;
						 magnitude = Math.random()*0.3;
						 
						 firstconn[i][j]=magnitude;
						 
						 if(sign==0)
							 firstconn[i][j] *= -1;			 	
						
					 }
					 
				 }
				 secondconn=new double[hiddenno];
				 secondsecondconn=new double[hiddenno];
				 for(int j=0;j<hiddenno;j++)
				 {
					 if(Math.random()<0.5)
						 sign=0;
					 else
						 sign=1;
					 magnitude = Math.random()*0.3;
					 
					 secondconn[j]=magnitude;
					 
					 if(sign==0)
						 secondconn[j] *= -1;
					 				 
				 }
				 
			 
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		  
	  }
	  
	  
	  public static double sigmoid(double input)
	  {
		  return 1/(1+Math.pow(Math.E,(-1 * input)));
	  }
	  
	  
	  public static void backPropagate(int iterno, int featureSelect[])
	  {
		  //all the operations are done only if featureSelect[attrbNo]=1 and not if it is 
		  try
		  {
			  double outputerror=0;
			  double tempsecondconn[]=new double[hiddenno];
			  double tempfirstconn[][]=new double[inputno][hiddenno];
			  double hiddenErrors[]=new double[hiddenno];
			  double M;			 
			  
			  if(iterno==0)
				  M=0;
			  else
				  M=MomentumFactor;
			  
			  outputerror=(trainingRows.getFloat("att"+(inputno+1))-finalOut)*(1-finalOut)*finalOut;//changed it from att25
			  
			  for(int j=0;j<hiddenno;j++)// Computing new weights for output layer
			  {
				  tempsecondconn[j]=secondconn[j]+((1-M)*(learningRate*outputerror*hiddenOut[j]))+(M*(secondconn[j]-secondsecondconn[j]));
			  }
			  secondsecondconn=secondconn;
			  secondconn=tempsecondconn;
			  
			  for(int j=0;j<hiddenno;j++)// Computing errors for hidden layer
			  {
				  hiddenErrors[j]=outputerror*secondconn[j]*(1-hiddenOut[j])*hiddenOut[j];
			  }
			  for(int i=0;i<inputno;i++)// New hidden layer weights
			  {
				  for(int j=0;j<hiddenno;j++)
				  {
					  if(featureSelect[i]!=0)//added
						  tempfirstconn[i][j]=firstconn[i][j]+((1-M)*(learningRate*hiddenErrors[j]*trainingRows.getFloat("att"+(i+1))))+(M*(firstconn[i][j]-secondfirstconn[i][j]));//changed it from j+1 to i+1
					  else
						  tempfirstconn[i][j]=firstconn[i][j];
				  }
			  }
			  
			  secondfirstconn=firstconn;
			  firstconn=tempfirstconn;
		  }
		  catch(Exception e)
		  {
			  e.printStackTrace();
		  }
	  }
	  
	  
	  public static double createAndTrain(int noOfIterations,int featureSelect[],int print)//print=0 no printing of the accuracy else print the accuracy
	  { 
			  try
			  {
				  
		  	  createNeuralNetwork();
			  
			  //train the neural net similar to earlier
			  
			  //all the operations are done only if featureSelect[attrbNo]=1 and not if it is 0			  
			  
		  	  //System.out.println("The trianrowno is:"+trainrowsno);
			  Class.forName("com.mysql.jdbc.Driver");
				 // setup the connection with the DB.
			  connect = DriverManager.getConnection("jdbc:mysql://localhost/semproject?" + "user=ggk&password=ggk");
				 
			  statement = connect.createStatement();			  
			  trainingRows = statement.executeQuery("select * from " + table + " limit " + trainrowsno);
			  
			  int i=0;
			  double error=0;			  
			  hiddenOut = new double[hiddenno];
			  double finalOutBeforeSig;			  
			  double beforeSig;
			  int flag=0;
			  
			  while(i < noOfIterations)
			  {				  
				  
				  trainingRows.next();				 
				  for(int j=0;j < hiddenno; j++)
				  {
					  beforeSig = 0;
					  for(int k=0;k<inputno;k++)
					  {
						  //System.out.println("featureselect[k] is:"+featureSelect[k]+" and k is:"+k);
						  //System.out.println("train[1] is:"+trainingRows.getFloat("att1"));
						  //System.out.println("train[2] is:"+trainingRows.getFloat("att2"));
						  if(featureSelect[k]!=0)
							  beforeSig += trainingRows.getFloat("att"+(k+1)) * firstconn[k][j];//changed to att k+1 from att j+1				  
					  }					  
					 
					  hiddenOut[j] = sigmoid(beforeSig);
				  }
				  
				  finalOutBeforeSig = 0;
				  
				  for(int j=0;j<hiddenno;j++)
				  {
					  finalOutBeforeSig += hiddenOut[j] * secondconn[j];
				  }
				  
				  finalOut = sigmoid(finalOutBeforeSig);
				  
				  error += Math.abs(finalOut-trainingRows.getFloat("att"+(inputno+1)));//changed att25 to this
				  
				  
				  backPropagate(i,featureSelect);
				  
				  if(i%100==0&&flag==0&&i!=0&&print==1)
				  {
					  flag=1;
					  System.out.println("");
					  System.out.println("The average at "+i);
					  for(int j=0;j<inputno;j++)
					  {
						  float sum=0;
						  for(int k=0;k<hiddenno;k++)
						  {
							  if(featureSelect[j]!=0)
								  sum+=firstconn[j][k];
						  }
						  sum/=24;
						  System.out.print("\t\t"+sum );// this need not give right answer. 24 is not always true
					  }
					}
				  
				  if(trainingRows.isLast())
				  {					  
					  trainingRows.beforeFirst();
					  error /= totrows;
					  if(error < minError)
						  break;
					  if(i%100==0)
						  //System.out.println(i+" and the error is:"+error);
					  error=0;
					  i++;
					  flag=0;
				  }
			  }
			  
			    
		  }
		  catch(Exception e)
		  {
			  e.printStackTrace();
		  }
		  return validate(featureSelect,print);
		  //return 0.0  		  
	  }
	  
	  
	  public static double validate(int featureSelect[], int print)
	  {
		  try
		  {
			  truePositive = 0;
			  trueNegative = 0;
			  TotalTruePositive = 0;
			  TotalTrueNegative = 0;
			  
			  Class.forName("com.mysql.jdbc.Driver");
				 // setup the connection with the DB.
			  connect = DriverManager.getConnection("jdbc:mysql://localhost/semproject?" + "user=ggk&password=ggk");
				 
			  statement = connect.createStatement();
			  
			  allrows = statement.executeQuery("select * from " + table);
			  
			  hiddenOut = new double[hiddenno];
			  double finalOutBeforeSig;			  
			  double beforeSig;
			  int i=0;
			  
			  while(allrows.next())
			  {
				  for(int j=0;j < hiddenno; j++)
				  {
					  beforeSig = 0;
					  for(int k=0;k<inputno;k++)
					  {
						  if(featureSelect[k]!=0)
							  beforeSig += allrows.getFloat("att"+(k+1)) * firstconn[k][j];	//changed it from att j+1 to att k+1			  
					  }	  
					  
					  hiddenOut[j] = sigmoid(beforeSig);
				  }
				  
				  finalOutBeforeSig = 0;
				  
				  for(int j=0;j<hiddenno;j++)
				  {
					  finalOutBeforeSig += hiddenOut[j] * secondconn[j];
				  }
				  
				  finalOut = sigmoid(finalOutBeforeSig);
				  if(finalOut>0.5 && (allrows.getFloat("att"+(inputno+1))==1))//changed it from att25
				  {
					  TotalTruePositive++;
					  if(i<trainrowsno)
						  truePositive++;
				  }
				  if(finalOut<0.5 && (allrows.getFloat("att"+(inputno+1))==0))//changed it from att25
				  {
					  TotalTrueNegative++;
					  if(i<trainrowsno)
						  trueNegative++;
				  }
				  i++;
			  }
			  if(print==1)
			  {
				  System.out.println("");
				  System.out.println("The Total true positive is:"+TotalTruePositive+ " and the Total true negative is:"+TotalTrueNegative);
				  System.out.println("The Training true positive is:"+truePositive+ " and the Training true negative is:"+trueNegative);
				  System.out.println("The totrews is:"+totrows);
				  System.out.println("The acuuracy is:"+ 1.0*(TotalTruePositive+TotalTrueNegative)/totrows);
			  }
			  return 1.0*(TotalTruePositive+TotalTrueNegative)/totrows;
		  }
		  catch(Exception e)
		  {
			  e.printStackTrace();
		  }
		//all the operations are done only if featureSelect[attrbNo]=1 and not if it is 0
		  
		  //returns the  total true accuracy
		  return 0.0;//remove this. just to remove error
	  }
	  
	  
	  public static void sortByFitness()
	  {
		  for(int i=0;i<initPopNo;i++)
		  {
			  for(int j=0;j<initPopNo-1;j++)
			  {
				  if(fitness[j+1]<fitness[j])
				  {
					  int temp_population[]= new int[inputno];
					  temp_population=population[j];
					  population[j]=population[j+1];
					  population[j+1]=temp_population;
					  
					  double temp=fitness[j];					  					  
					  fitness[j]=fitness[j+1];					  	  
					  fitness[j+1]=temp;
					  					  
				  }
			  }
		  }
	  }
	  
	  public static void crossover()
	  {
		  //Assuming fitness sorted in ascending order... taking last 3
		  
		  int newpopulation[][] = new int[initPopNo][inputno];
		  for(int i=(initPopNo/2);i<=(initPopNo-1);i++)
		  {
			  for(int j=0;j<inputno;j++)
			  {
				  newpopulation[i][j]=population[i][j];
			  }
		  }
		  
		  //Assuming fitness value double
		  //Roulette wheel
		  double sum=0.0;
		  double probability[]=new double[initPopNo];
		  double sum_prob=0.0;
		  int chosen[] = new int[2]; 
		  for(int i=0;i<initPopNo;i++)
		  {
			  sum += fitness[i];
		  }
		  
		  for(int i=0;i<initPopNo;i++)
		  {
			  probability[i] = sum_prob + ((fitness[i])/sum);
			  sum_prob = probability[i];
		  }
		  
		  Random randomGenerator = new Random();
		  
		  for(int i=0;i<(initPopNo/2);i++)
		  {
			  for(int j=0;j<2;j++) // Choosing 2 parents
			  {
				  double random_no = Math.random();
				  if(random_no<probability[0])
					  chosen[j]=0;
				  for(int k=0;k<5;k++)
				  {
					  if(random_no>=probability[k] && random_no<=probability[k+1])
						  chosen[j]=k+1;
				  }
			  }
			  
			  int CrossOverPoint = randomGenerator.nextInt(inputno);
			  
				  for(int l=0;l<=CrossOverPoint;l++)
				  {
					  newpopulation[i][l]=population[chosen[0]][l];
				  }
				  for(int m=CrossOverPoint+1;m<inputno;m++)
				  {
					  newpopulation[i][m]=population[chosen[1]][m];
				  }
				  if(Math.random()<mutateRate)
					  mutate(i);
		  }
		  
		  population=newpopulation;
	  }
	  
	  public static void mutate(int i)	  
	  {
		  //Assuming 1/3rd of no of bits as upper limit on no of bits to mutate
		  Random randomGenerator = new Random();
		  
		  int no_bits_to_mutate = randomGenerator.nextInt((int)(inputno/3));
		  
		  for(int j=0;j<no_bits_to_mutate;j++)
			  {
				  int position = randomGenerator.nextInt(inputno);
				  population[i][position]=(population[i][position]==1)?0:1;
			  }		  
	  }
	  
	  public static void GA(int iterations)
	  {
		  int i = 0;		  
		  while(i<iterations)
		  {
			  for(int j=0;j<population.length;j++)
			  {
				  //System.out.println("The value of j is"+j);
				  double temp=1.0;
				  temp=createAndTrain(GaTrainIterations,population[j],0);
				  //System.out.println("");
				  //System.out.println("The value of temp is:"+temp);
				  fitness[j] = temp;
			  }
			  
			  sortByFitness();
			  
			  System.out.println("The iteration is:"+i);
			  for(int j=0;j<initPopNo;j++)			  
				  System.out.print(" The firness of "+j+" is "+fitness[j]);
			  System.out.println("");
				
			  crossover();			  
			  
			  i++;
		  }
		  for(int j=0;j<population.length;j++)
		  {
			  fitness[j] = createAndTrain(GaTrainIterations,population[j],0);
		  }
		  sortByFitness();
		  for(int j=0;j<initPopNo;j++)			  
			  System.out.print("The firness of "+j+" is "+fitness[j]);
				  
		  finalpopulation = population[0];
		  
		  System.out.println("");
		  System.out.println("The final population is:");
		  for(int l=0;l<inputno;l++)
		  {
			  System.out.print(finalpopulation[l]);
		  }
		  System.out.println("");
		  
	  }
	  
	  
	  public static void main(String[] args)
	  {
		  intializations();
		  createIntialPopulation();		  
		  GA(30);	  
		  createAndTrain(NeuralTrainIterations,finalpopulation,1);		  
			  		  
	  }
}

