package io.oasp.gastronomy.restaurant.salesmanagement.dataaccess.impl.dao;

import io.oasp.gastronomy.restaurant.offermanagement.dataaccess.api.DrinkEntity;
import io.oasp.gastronomy.restaurant.offermanagement.dataaccess.api.dao.DrinkDao;
import io.oasp.module.configuration.common.api.ApplicationConfigurationConstants;
import io.oasp.module.jpa.dataaccess.api.RevisionMetadata;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * Test class to test the {@link DrinkDao}.
 *
 * @author jmetzler
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ContextConfiguration({ ApplicationConfigurationConstants.BEANS_DATA_ACCESS,
"classpath:/config/app/service/beans-test-service-rest.xml" })
@ActiveProfiles("db-plain")
public class DrinkDaoTest extends Assert {

  @Inject
  DrinkDaoTestBean testBean;

  /**
   * Test to check if the DrinkEntity is audited. All steps are executed in separate transactions in order to actually
   * write to the database. Otherwise hibernate envers won't work.
   */
  @Test
  public void checkAudit() {

    DrinkEntity drink = this.testBean.create();
    long drinkId = drink.getId();
    this.testBean.update(drinkId);
    this.testBean.verify(drinkId);

  }

  /**
   *
   * This type provides methods in a transactional environment for the containing test class. All methods, annotated
   * with the {@link Transactional} annotation, are executed in separate transaction, thus one test case can execute
   * multiple transactions.
   *
   * @author jmetzler
   */
  static class DrinkDaoTestBean {

    private final String description = "some description";

    private final String changedDescription = "some changed description";

    @Inject
    private DrinkDao drinkDao;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public DrinkEntity create() {

      DrinkEntity drink = new DrinkEntity();
      drink.setAlcoholic(false);
      drink.setDescription(this.description);
      drink.setName("some name");
      assertNull(drink.getId());
      drink = this.drinkDao.save(drink);
      return drink;
    }

    @Transactional
    public void update(long id) {

      DrinkEntity drink = this.drinkDao.find(id);
      drink.setAlcoholic(true);
      drink.setDescription(this.changedDescription);
      this.drinkDao.save(drink);
    }

    @Transactional
    public void verify(long id) {

      AuditReader auditReader = AuditReaderFactory.get(this.entityManager);

      assertTrue(auditReader.isEntityClassAudited(DrinkEntity.class));

      List<Number> revisions = auditReader.getRevisions(DrinkEntity.class, id);
      assertEquals(2, revisions.size());

      List<RevisionMetadata> history = this.drinkDao.getRevisionHistoryMetadata(id);
      assertEquals(2, history.size());

      // get first revision
      Number rev = history.get(0).getRevision();
      DrinkEntity drink = this.drinkDao.load(id, rev);
      assertTrue(drink.getDescription().equals(this.description));

      // get second revision
      rev = history.get(1).getRevision();
      drink = this.drinkDao.load(id, rev);
      assertTrue(drink.getDescription().equals(this.changedDescription));
    }

    /**
     * @param entityManager the entityManager to set
     */
    public void setEntityManager(EntityManager entityManager) {

      this.entityManager = entityManager;
    }
  };
}
