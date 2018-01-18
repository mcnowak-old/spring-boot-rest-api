package com.codecool.krk.gamesapi.studio;

import com.codecool.krk.gamesapi.exception.NoSuchIdException;
import com.codecool.krk.gamesapi.game.Game;
import com.codecool.krk.gamesapi.game.GameRepository;
import com.codecool.krk.gamesapi.logger.LoggerCreator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Set;

@Primary
@Service
public class StudioServiceImplArchive implements StudioService {

    private StudioRepository studioRepository;
    private GameRepository gameRepository;
    private LoggerCreator logger;

    public StudioServiceImplArchive(StudioRepository studioRepository, GameRepository gameRepository, LoggerCreator logger) {
        this.studioRepository = studioRepository;
        this.gameRepository = gameRepository;
        this.logger = logger;
    }

    @Override
    public Iterable<Studio> findAllStudios() {
        Iterable<Studio> studios = this.studioRepository.findAllByArchivedIsFalse();

        for (Studio studio : studios) {
            Set<Game> games = this.gameRepository.findAllByStudioAndArchivedIsFalse(studio);
            studio.setGames(games);
        }

        this.logger.logInfo("Listed all studios.");
        return studios;
    }

    @Override
    public Studio findStudioById(Integer id) throws NoSuchIdException {
        Studio studio = this.studioRepository.findStudioByIdAndArchivedIsFalse(id);

        if(studio == null) {
            throw new NoSuchIdException();
        }

        Set<Game> games = this.gameRepository.findAllByStudioAndArchivedIsFalse(studio);
        studio.setGames(games);

//        this.logger.logError("Listed some studio.");
        return studio;
    }

    @Override
    public void saveStudio(Studio studio) {
        Integer id = studio.getId();

        if (id != null) {
            Studio studioInDatabase = this.studioRepository.findOne(id);

            if (studioInDatabase != null) {
                studio.setId(null);
            }
        }

        this.studioRepository.save(studio);
    }

    @Override
    public void deleteStudio(Integer id) throws NoSuchIdException {

        Studio studio = this.studioRepository.findStudioByIdAndArchivedIsFalse(id);

        if(studio == null) {
            throw new NoSuchIdException();
        }

        studio.setArchived(true);

        for (Game game : studio.getGames()) {
            game.setArchived(true);
        }

        this.studioRepository.save(studio);
    }

    @Override
    public void updateStudio(Studio studio) throws NoSuchIdException {
        Studio databaseStudio = this.studioRepository.findOne(studio.getId());

        if (databaseStudio == null || databaseStudio.getArchived()) {
            throw new NoSuchIdException();
        }

        studio.setGames(databaseStudio.getGames());
        this.studioRepository.save(studio);
    }
}
