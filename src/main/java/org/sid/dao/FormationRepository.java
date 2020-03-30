package org.sid.dao;


import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

import javax.persistence.Table;

import org.sid.entities.Client;
import org.sid.entities.Formation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import org.springframework.transaction.annotation.Transactional;

@RepositoryRestResource
public interface FormationRepository extends JpaRepository<Formation, Long> {
	@Query("select f from Formation f where user_id like :x")
	public List<Formation> findByUserId(@Param("x") Long id);
	@Query("select f from Formation f where Article_Cat like :x")
	public List<Formation> findByArticleCat(@Param("x") String cat);
	@Query("select count(*) from Formation f where Article_Cat like :x")
	public Long countByArticleCat(@Param("x") String cat);
	@Transactional
	@Modifying
	@Query(value="insert into Formationreservee (user_id,training_id) values (:userId,:trainingId)",nativeQuery = true)
	public void insertIntoReservation(@Param("userId") Long uId,@Param("trainingId") Long tId);
	@Query(value="select count(*) from Formationreservee f where training_id like :x",nativeQuery = true)
	public Long countByIdFormation(@Param("x") Long id);
	@Query(value="select training_id from Formationreservee f where  user_id like :y",nativeQuery = true)
	public List<Long> verifyIfAlreadyExist(@Param("y") Long uId);
	@Query(value="select * from Formation fo  where  id in (select training_id from formationreservee f where user_id like :x)",nativeQuery = true)
	public List<Formation> findReservedTraining(@Param("x") Long id);
	@Transactional
	@Modifying
	@Query(value="delete from formationreservee where training_id like :x and user_id like :y",nativeQuery = true)
	public void deleteMyReservation(@Param("x") Long tId,@Param("y") Long uId);

	
	
}
