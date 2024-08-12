package eolopark.server.service;

import eolopark.server.model.internal.Report;
import eolopark.server.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

/**
 * This service is used to save on database the updates on reports
 * <p>
 * To notify users, NotificationService will be used
 */
@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService (ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Long saveReport (String username) {

        Report report = new Report(username, 0, false);
        reportRepository.save(report);
        return report.getId();
    }

    public Report replaceReportByQueueMessage (Long reportId, int progress, boolean completed) {
        Report report = reportRepository.findById(reportId).orElseThrow(NoSuchElementException::new);
        report.setProgress(progress);
        report.setCompleted(completed);
        reportRepository.save(report);
        return report;
    }

    public Report getReportById (Long reportId) {
        return reportRepository.findById(reportId).orElseThrow(NoSuchElementException::new);
    }
}