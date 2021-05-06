package net.khalegh.batsapp.dao;


import net.khalegh.batsapp.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepo extends JpaRepository<Contact, Long> {
}
