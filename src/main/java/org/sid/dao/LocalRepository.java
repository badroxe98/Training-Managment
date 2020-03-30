package org.sid.dao;
import java.util.List;


import org.sid.entities.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface  LocalRepository extends JpaRepository<Local, Long>
{
	@Query(value="select * from Local l where owner like :x",nativeQuery = true)
	public List<Local> findByUserId(@Param("x") Long id);
	@Query(value="select * from Local l where ville like :x",nativeQuery = true)
	public List<Local> findByCity(@Param("x") String ville);
	

}
