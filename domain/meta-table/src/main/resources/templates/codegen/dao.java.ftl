package ${packageBase}.dao;

import ${packageBase}.domain.${entityName};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ${entityName}Dao extends JpaRepository<${entityName}, Long>, JpaSpecificationExecutor<${entityName}> {
}
