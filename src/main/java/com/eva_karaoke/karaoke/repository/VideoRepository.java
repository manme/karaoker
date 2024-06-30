package com.eva_karaoke.karaoke.repository;

import com.eva_karaoke.karaoke.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
}
