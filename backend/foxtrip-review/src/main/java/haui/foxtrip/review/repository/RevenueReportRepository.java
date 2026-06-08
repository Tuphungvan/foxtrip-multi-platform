package haui.foxtrip.review.repository;

import haui.foxtrip.review.domain.RevenueReport;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RevenueReportRepository extends JpaRepository<RevenueReport, UUID> {
    Optional<RevenueReport> findByYearAndMonth(Integer year, Integer month);

    List<RevenueReport> findTop12ByOrderByYearDescMonthDesc();
}
