package xietongguolv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

//测试集长度：124939,训练集长度：875270
//movie number is3698
//
//precision=%16.812125249833446	recall=%8.07914262159934	coverage=%53.40724716062737	popularity=6.8203023242082255
//
//precision=%20.479680213191205	recall=%9.841602702118633	coverage=%41.69821525148729	popularity=6.978235711167304
//
//precision=%22.88640906062625	recall=%10.998167105547507	coverage=%32.07138994050838	popularity=7.108795189334126
//
//precision=%24.53530979347102	recall=%11.790553790249641	coverage=%25.959978366684695	popularity=7.205357835196816
//
//precision=%25.163224516988674	recall=%12.09230104290894	coverage=%20.146024878312602	popularity=7.2932510888075015


public class Jiyuyonghudexietongguolvsuanfa {
	HashMap<Integer,Set<Integer>> trainset=new HashMap<Integer,Set<Integer>>();
	HashMap<Integer,Set<Integer>> testset=new HashMap<Integer,Set<Integer>>();
	HashMap<Integer,Set<Integer>> inverse_table=new HashMap<Integer,Set<Integer>>();
	HashMap<Integer,Integer> movie_popular=new HashMap<Integer,Integer>();
	int i=0;
	int trainset_length;
	int testset_length;
	int user_sim_mat[][];
	double  user_simlarity[][];
	int movie_count=0;
	List<Rank> recommendedMoviesList=null;
	List<Ralated_user> ralatedUsersList=null;
    int k=0;
    int temp_k=0;
	int n=10;
	int temp_n=10;
	Random random=new Random(0);
	public void generate_dataset(int pivot) throws IOException{
		
		File file=new File("E:\\workspace\\ml-1m\\ratings.dat");		
		if(!file.exists()||file.isDirectory())
			throw new FileNotFoundException();		
		BufferedReader br=new BufferedReader(new FileReader(file));
		String temp=null;		
		while ((temp=br.readLine())!=null) {
			
			String[] content=temp.replaceAll("\n\t", "").split("::");
			if(random.nextInt(8)==pivot){
				if(testset.containsKey(Integer.parseInt(content[0]))){
					HashSet<Integer> set =(HashSet<Integer>) testset.get(Integer.parseInt(content[0]));
					set.add(Integer.parseInt(content[1]));
					testset.put(Integer.parseInt(content[0]),set);
				}else{
					Set<Integer> set=new HashSet<Integer>();
					set.add(Integer.parseInt(content[1]));
					testset.put(Integer.parseInt(content[0]),set);
				}
				testset_length++;
				
			}else{
				if(trainset.containsKey(Integer.parseInt(content[0]))){
					HashSet<Integer> set =(HashSet<Integer>) trainset.get(Integer.parseInt(content[0]));
					set.add(Integer.parseInt(content[1]));
					trainset.put(Integer.parseInt(content[0]),set);
					
				}else{
					Set<Integer> set=new HashSet<Integer>();
					set.add(Integer.parseInt(content[1]));
					trainset.put(Integer.parseInt(content[0]),set);
				}
				
				trainset_length++;
				
			}
			i++;
			if (i%100000 == 0)
                System.out.println("已装载"+i+"文件");
	   }
		System.out.println("测试集和训练集分割完成，测试集长度："+testset_length+",训练集长度："+trainset_length);
		
	}
	
	// build inverse table for item-users
    // key=movieID, value=list of userIDs who have seen this movie
	public void calc_user_sim(){
		
		for(int obj : trainset.keySet()){ 
			
			Set<Integer> value = trainset.get(obj );
			Iterator<Integer> it=value.iterator();
			
		       while(it.hasNext())
		       {
		           int o=it.next();
		           if(inverse_table.containsKey(o)){
		        	   Set<Integer> set=inverse_table.get(o);
		        	   set.add(obj);
		        	   inverse_table.put(o,set);
					}else {
						Set<Integer> set=new HashSet<Integer>();
						set.add(obj);
						inverse_table.put(o,set);
					}
		           //  count item popularity at the same time
		           if(!movie_popular.containsKey(o)){
		        	   movie_popular.put(o,1);
		           }else {
		        	   movie_popular.put(o,movie_popular.get(o)+1);
				   }		          
		       }			
			}
		System.out.println("inverse——table创建成功");
		//建立反转表的目的是方便建立co-rated movies 矩阵
		movie_count=inverse_table.size();
		System.out.println("movie number is"+movie_count);		
		System.out.println("building user co-rated movies matrix...");
		
		user_sim_mat=new int[trainset.size()+1][trainset.size()+1];	
		
		for(int movie : inverse_table.keySet()){		
			Set<Integer> users=inverse_table.get(movie);
			Iterator<Integer> u=users.iterator();
			while(u.hasNext()){
				int i=u.next();
				Iterator<Integer> v=users.iterator();
				while(v.hasNext()){
					int j=v.next();
					if(i==j){
						continue;
					}else {
						user_sim_mat[i][j]+=1;						
					}					
				}
			}
		}
		
		System.out.println("co-rated movies矩阵创建成功");
		//calculate similarity matrix
		System.out.println("calculating user similarity matrix...");
	
		user_simlarity=new double[trainset.size()+1][trainset.size()+1];
		for(int i=0;i<trainset.size()+1;i++)
			for(int j=0;j<trainset.size()+1;j++){
				if(user_sim_mat[i][j]==0)
					continue;
				user_simlarity[i][j]=user_sim_mat[i][j]/Math.sqrt(trainset.get(i).size()*trainset.get(j).size()*1.0);
				
			}
		
		System.out.println("user_simlarity矩阵创建成功");			
	 
	}
	//推荐相似度最大的前k个user,和前 N个movie
	public void recommend(int user){
		ralatedUsersList=new ArrayList<Ralated_user>();
		
		for(int i=0;i<trainset.size()+1;i++){
			if(user_simlarity[user][i]==0.0)
				continue;
			Ralated_user r=new Ralated_user();
			r.setId(i);
			r.setSimlarity(user_simlarity[user][i]);
			ralatedUsersList.add(r);
		}
		
		if(ralatedUsersList.size()>k){
			Heapsort heapsort=new Heapsort();
			heapsort.sort(ralatedUsersList,k);
		}
		if(ralatedUsersList.size()>0){
		Set<Integer> watched_movies=trainset.get(user);
		recommendedMoviesList=new ArrayList<Rank>();
		
		
		if(ralatedUsersList.size()<=k){
			k=ralatedUsersList.size();
		}			
		for(int i=0;i<k ;i++){
			Ralated_user u=(Ralated_user) ralatedUsersList.get(i);
			Set<Integer> movie = trainset.get(u.getId());
			Iterator<Integer> it=movie.iterator();
			while(it.hasNext()){
				int j=it.next();
				if(watched_movies.contains(j)){
					continue;
				}else {
					Rank r=new Rank();
					r.setMovie(j);
					r.setSum_simlatrity(u.getSimlarity());
					//如果已经存在，那么就修改其值
					int index=recommendedMoviesList.indexOf(r);
					if(index>-1){
						Rank rr=(Rank) recommendedMoviesList.get(index);
						rr.setSum_simlatrity(rr.getSum_simlatrity()+u.getSimlarity());
					}else {
						recommendedMoviesList.add(r);
					}
				}
			}
		}
		}
		k=temp_k;
		if(recommendedMoviesList.size()>n){
		Heapsort ss=new Heapsort();
		ss.sort(recommendedMoviesList, n);//堆排序
		}
		
		
	}
	
	public void evaluate(){
		int rec_count=0;
		int test_count=0;
		int hit=0;
		double popularSum=0;
		Set<Integer> all_rec_movies=new HashSet<Integer>();
		Iterator<Integer> it=trainset.keySet().iterator();
		while(it.hasNext()){
			int user=it.next();
			if(user%500==0)
				System.out.println("已经推荐了"+user+"个用户");
			
			Set<Integer> test_movies=testset.get(user);
			recommend(user);
			
			if(recommendedMoviesList!=null&&test_movies!=null){
				if(recommendedMoviesList.size()<n) 
					n=recommendedMoviesList.size();
			for(int i=0;i<n;i++){
				Rank rec_movie=recommendedMoviesList.get(i);
				if(test_movies.contains(rec_movie.getMovie())){
					hit++;
				}
				all_rec_movies.add(rec_movie.getMovie());
				popularSum+=Math.log(1+movie_popular.get(rec_movie.getMovie()));
			}
			
			rec_count+=n;
			test_count+=test_movies.size();
			}
			n=temp_n;
			
		}
		
		double precision=hit/(1.0*rec_count);
		double recall=hit/(1.0*test_count);
		double coverage=all_rec_movies.size()/(1.0*movie_count);
		double popularity=popularSum/(1.0*rec_count);
		System.out.println("precision=%"+precision*100+"\trecall=%"+recall*100+"\tcoverage=%"+coverage*100+"\tpopularity="+popularity);
		
	}
	
	
	public static void main(String[] args) throws IOException {
		Jiyuyonghudexietongguolvsuanfa ss=new Jiyuyonghudexietongguolvsuanfa();
		ss.generate_dataset(3);
		ss.calc_user_sim();
		ArrayList<Integer> list=new ArrayList<Integer>();
		list.add(5);
		list.add(10);
		list.add(20);
		list.add(40);
		list.add(80);
		list.add(160);
		for (int i = 0; i <list.size() ; i++) {
			ss.k=list.get(i);
			ss.temp_k=list.get(i);
			ss.evaluate();
		}

	}

}
